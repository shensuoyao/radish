package org.sam.shen.scheduing.cluster;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * 集群上下文管理
 * @author suoyao
 * @date 2019年3月1日 下午4:13:28
  * 
 */
@Slf4j
public class ClusterCnxManager {

	private ClusterPeer self;
	
	/*
	 * Listener thread
	 */
	public final Listener listener;
	
	/*
	 * Shutdown flag
	 */
	volatile boolean shutdown = false;
	
	static final int SEND_CAPACITY = 1;
	
	// 接受消息的最多容量
	static final int RECV_CAPACITY = 100;
	
	static final int PACKETMAXSIZE = 1024 * 512;
	
	/*
	 * 连接超时的毫秒值
	 */
	private int cnxTO = 5000;
	
	// 接收到的消息队列
	public final ArrayBlockingQueue<Message> recvQueue;
	private final Object recvQLock = new Object();
	
	// 发送队列
	final ConcurrentHashMap<Integer, ArrayBlockingQueue<ByteBuffer>> queueSendMap;
	// 每台节点服务器对应一个SendWorker
	final ConcurrentHashMap<Integer, SendWorker> senderWorkerMap;
	final ConcurrentHashMap<Integer, ByteBuffer> lastMessageSent;
	
	// 线程数量
	private AtomicInteger threadCnt = new AtomicInteger(0);
	
	public ClusterCnxManager(ClusterPeer self) {
		recvQueue = new ArrayBlockingQueue<>(RECV_CAPACITY);
		this.queueSendMap = new ConcurrentHashMap<>();
		this.senderWorkerMap = new ConcurrentHashMap<>();
		this.lastMessageSent = new ConcurrentHashMap<>();
		if(self.getCnxTimeout() <= 0) {
			this.cnxTO = 5000;
		}
		this.self = self;
		listener = new Listener();
	}
	
	/**
	 * 监听选举端口地址
	 * @author suoyao
	 * @date 2019年3月1日 下午4:35:52
	  *
	 */
	public class Listener extends Thread {

		// 监听服务器
		volatile ServerSocket ss = null;

		@Override
		public void run() {
			int numRetries = 0; // 重试次数
			InetSocketAddress addr;
			while ((!shutdown) && (numRetries < 3)) {
				try {
					ss = new ServerSocket();
					ss.setReuseAddress(true);
					if (self.isClusterListenOnAllIPs()) {
						// 监听本机所有可用IP地址
						int port = self.clusterServers.get(self.getMyId()).electionAddr.getPort();
						addr = new InetSocketAddress(port);
					} else {
						// 监听本机设置的IP
						addr = self.clusterServers.get(self.getMyId()).electionAddr;
					}
					log.info("My election bind port: " + addr.toString());
					setName(addr.toString());
					ss.bind(addr);
					while (!shutdown) {
						Socket client = ss.accept();
						setSockOpts(client);
						log.info("Received connection request " + client.getRemoteSocketAddress());
						receiveConnection(client);
						numRetries = 0;
					}
				} catch (IOException e) {
					log.error("Exception while listening", e);
					numRetries++;
					try {
						ss.close();
						Thread.sleep(1000);
					} catch (IOException ie) {
						log.error("Error closing server socket", ie);
					} catch (InterruptedException ie) {
						log.error("Interrupted while sleeping. " + "Ignoring exception", ie);
					}
				}
			}
			log.info("Leaving listener");
			if (!shutdown) {
				log.error("As I'm leaving the listener thread, " + "I won't be able to participate in leader "
				        + "election any longer: " + self.clusterServers.get(self.getMyId()).electionAddr);
			}
		}

		/**
		 * Halts this listener thread.
		 */
		void halt() {
			try {
				log.debug("Trying to close listener: " + ss);
				if (ss != null) {
					log.debug("Closing listener: " + self.getId());
					ss.close();
				}
			} catch (IOException e) {
				log.warn("Exception when shutting down listener: " + e);
			}
		}
	}
	
	/**
	 * Flag that it is time to wrap up all activities and interrupt the listener.
	 */
	public void halt() {
		shutdown = true;
		log.debug("Halting listener");
		listener.halt();

		softHalt();
	}
   
	/**
	 * A soft halt simply finishes workers.
	 */
	public void softHalt() {
		for (SendWorker sw : senderWorkerMap.values()) {
			log.debug("Halting sender: " + sw);
			sw.finish();
		}
	}
	
	/**
	 * Helper method to set socket options.
	 * 
	 * @param sock Reference to socket
	 */
	private void setSockOpts(Socket sock) throws SocketException {
		sock.setTcpNoDelay(true);
		sock.setSoTimeout(self.tickTime * self.syncLimit);
	}
	
	private void closeSocket(Socket sock) {
		try {
			sock.close();
		} catch (IOException ie) {
			log.error("Exception while closing", ie);
		}
	}
	
	/**
	 * 消息体
	 * @author suoyao
	 * @date 2019年3月1日 下午5:36:16
	  * 
	 */
	static public class Message {

		Message(ByteBuffer buffer, int nid) {
			this.buffer = buffer;
			this.nid = nid;
		}

		ByteBuffer buffer;
		int nid;
	}

	/**
	 * 如果此服务器收到连接请求，则判断新连接的服务器节点ID是否小于自己的节点ID。 
	 * 只允许ID大的向ID小的主动请求连接，这样做是为了避免重复的连接开销
	 * (请注意，它会检查是否已与此服务器建立连接) 
	 * 如果是，则它发送尽可能小的长值以断开连接。
	 * @author suoyao
	 * @date 下午5:07:16
	 * @param sock
	 * @return
	 */
	public boolean receiveConnection(Socket sock) {
		Integer nid = null;
		try {
			// Read server nid
			DataInputStream din = new DataInputStream(sock.getInputStream());
			nid = din.readInt();
			if (nid < 0) {	// 节点ID为大于零的整数
				nid = din.readInt();
				// 从信息中获取下一个整型字节
				int num_remaining_bytes = din.readInt();
				byte[] b = new byte[num_remaining_bytes];
				// 删除剩余的字节信息
				int num_read = din.read(b);
				if (num_read != num_remaining_bytes) {
					log.error("Read only " + num_read + " bytes out of " + num_remaining_bytes + " sent by server "
					        + nid);
				}
			}
		} catch (IOException e) {
			closeSocket(sock);
			log.warn("Exception reading or writing challenge: " + e.toString());
			return false;
		}

		// 如果对方节点的nid小于自己的nid则，断开连接，并主动同对方建立连接
		if (nid < self.getMyId()) {
			SendWorker sw = senderWorkerMap.get(nid);
			if (sw != null) {
				sw.finish();
			}

			if(log.isDebugEnabled()) {
				log.debug("Create new connection to server: " + nid);
			}
			closeSocket(sock);
			connectOne(nid);

			// 否则开启工作线程接收数据
		} else {
			SendWorker sw = new SendWorker(sock, nid);
			RecvWorker rw = new RecvWorker(sock, nid, sw);
			sw.setRecvWork(rw);

			SendWorker vsw = senderWorkerMap.get(nid);

			if (vsw != null) {
				vsw.finish();
			}

			senderWorkerMap.put(nid, sw);

			if (!queueSendMap.containsKey(nid)) {
				queueSendMap.put(nid, new ArrayBlockingQueue<>(SEND_CAPACITY));
			}

			sw.start();
			rw.start();

			return true;
		}
		return false;
	}
	
	public void toSend(Integer nid, ByteBuffer b) {
		/*
		 * If sending message to myself, then simply enqueue it (loopback).
		 */
		if (self.getId() == nid) {
			b.position(0);
			addToRecvQueue(new Message(b.duplicate(), nid));
			/*
			 * Otherwise send to the corresponding thread to send.
			 */
		} else {
			/*
			 * Start a new connection if doesn't have one already.
			 */
			if (!queueSendMap.containsKey(nid)) {
				ArrayBlockingQueue<ByteBuffer> bq = new ArrayBlockingQueue<>(SEND_CAPACITY);
				queueSendMap.put(nid, bq);
				addToSendQueue(bq, b);

			} else {
				ArrayBlockingQueue<ByteBuffer> bq = queueSendMap.get(nid);
				if (bq != null) {
					addToSendQueue(bq, b);
				} else {
					log.error("No queue for server " + nid);
				}
			}
			connectOne(nid);
		}
	}
	
	/**
	 * 尝试连接所有的节点服务器
	 * @author suoyao
	 * @date 下午4:00:41
	 */
	public void connectAll() {
		int nid;
		for (Enumeration<Integer> en = queueSendMap.keys(); en.hasMoreElements();) {
			nid = en.nextElement();
			connectOne(nid);
		}
	}
	
	/**
	 * 尝试用nid去连接对应的服务器
	 * @author suoyao
	 * @date 下午2:37:57
	 * @param nid
	 */
	synchronized void connectOne(int nid) {
		if (senderWorkerMap.get(nid) == null) {
			InetSocketAddress electionAddr;
			if (self.clusterServers.containsKey(nid)) {
				electionAddr = self.clusterServers.get(nid).electionAddr;
			} else {
				log.warn("Invalid server id: " + nid);
				return;
			}
			try {
				if (log.isDebugEnabled()) {
					log.debug("Opening channel to server " + nid);
				}
				Socket sock = new Socket();
				setSockOpts(sock);
				sock.connect(self.getView().get(nid).electionAddr, cnxTO);
				if (log.isDebugEnabled()) {
					log.debug("Connected to server " + nid);
				}
				initiateConnection(sock, nid);
			} catch (UnresolvedAddressException e) {
				log.warn("Cannot open channel to " + nid + " at election address " + electionAddr, e);
				throw e;
			} catch (IOException e) {
				log.warn("Cannot open channel to " + nid + " at election address " + electionAddr, e);
			}
		} else {
			log.debug("There is a connection already for server " + nid);
		}
	}
	
	/**
	 * 初始化连接后，发送本机的node ID
	 * @author suoyao
	 * @date 下午4:39:30
	 * @param sock
	 * @param nid
	 * @return
	 */
	public boolean initiateConnection(Socket sock, Integer nid) {
		DataOutputStream dout;
		try {
			// Sending nid and challenge
			dout = new DataOutputStream(sock.getOutputStream());
			dout.writeInt(self.getMyId());
			dout.flush();
		} catch (IOException e) {
			log.warn("Ignoring exception reading or writing challenge: ", e);
			closeSocket(sock);
			return false;
		}

		// If lost the challenge, then drop the new connection
		if (nid > self.getMyId()) {
			log.info("Have smaller server identifier, so dropping the " + "connection: (" + nid + ", " + self.getId()
			        + ")");
			closeSocket(sock);
		} else {
			// Otherwise proceed with the connection
			SendWorker sw = new SendWorker(sock, nid);
			RecvWorker rw = new RecvWorker(sock, nid, sw);
			sw.setRecvWork(rw);

			SendWorker vsw = senderWorkerMap.get(nid);

			if (vsw != null) {
				vsw.finish();
			}

			senderWorkerMap.put(nid, sw);
			if (!queueSendMap.containsKey(nid)) {
				queueSendMap.put(nid, new ArrayBlockingQueue<>(SEND_CAPACITY));
			}

			sw.start();
			rw.start();

			return true;
		}
		return false;
	}
	
	/**
	 * 发送工作线程
	 * @author suoyao
	 * @date 2019年3月4日 上午10:15:01
	  * 
	 */
	public class SendWorker extends Thread {
		Integer nid;
		Socket sock;
		RecvWorker rw;
		volatile boolean running = true;
		DataOutputStream dout;
		
		SendWorker(Socket sock, Integer nid) {
			super("SendWorker:" + nid);
			this.nid = nid;
			this.sock = sock;
			rw = null;
			try {
				dout = new DataOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.error("Unable to access socket output stream", e);
				closeSocket(sock);
				running = false;
			}
			log.debug("Address of remote peer: " + this.nid);
		}
		
		synchronized void setRecvWork(RecvWorker rw) {
			this.rw = rw;
		}
		
		synchronized RecvWorker getRecvWorker() {
			return rw;
		}
		
		synchronized boolean finish() {
			if (log.isDebugEnabled()) {
				log.debug("Calling finish for " + nid);
			}

			if (!running) {
				return running;
			}

			running = false;
			closeSocket(sock);

			this.interrupt();
			if (rw != null) {
				rw.finish();
			}

			if (log.isDebugEnabled()) {
				log.debug("Removing entry from senderWorkerMap nid=" + nid);
			}
			senderWorkerMap.remove(nid, this);
			threadCnt.decrementAndGet();
			return running;
		}
		
		synchronized void send(ByteBuffer b) throws IOException {
			byte[] msgBytes = new byte[b.capacity()];
			try {
				b.position(0);
				b.get(msgBytes);
			} catch (BufferUnderflowException be) {
				log.error("BufferUnderflowException ", be);
				return;
			}
			dout.writeInt(b.capacity());
			dout.write(b.array());
			dout.flush();
		}
		
		@Override
		public void run() {
			threadCnt.incrementAndGet();
			try {
				ArrayBlockingQueue<ByteBuffer> bq = queueSendMap.get(nid);
				if (bq == null || isSendQueueEmpty(bq)) {
					ByteBuffer b = lastMessageSent.get(nid);
					if (b != null) {
						log.debug("Attempting to send lastMessage to sid=" + nid);
						send(b);
					}
				}
			} catch (IOException e) {
				log.error("Failed to send last message. Shutting down thread.", e);
				this.finish();
			}

			try {
				while (running && !shutdown && sock != null) {

					ByteBuffer b = null;
					try {
						ArrayBlockingQueue<ByteBuffer> bq = queueSendMap.get(nid);
						if (bq != null) {
							b = pollSendQueue(bq, 1000, TimeUnit.MILLISECONDS);
						} else {
							log.error("No queue of incoming messages for " + "server " + nid);
							break;
						}

						if (b != null) {
							lastMessageSent.put(nid, b);
							send(b);
						}
					} catch (InterruptedException e) {
						log.warn("Interrupted while waiting for message on queue", e);
					}
				}
			} catch (Exception e) {
				log.warn("Exception when using channel: for nid " + nid + " my id = " + self.getMyId() + " error = " + e);
			}
			this.finish();
			log.warn("Send worker leaving thread");
		}
		
	}
	
	/**
	 * 接收工作线程
	 * @author suoyao
	 * @date 2019年3月4日 上午10:15:14
	  * 
	 */
	public class RecvWorker extends Thread {
		Integer nid;
		Socket sock;
		volatile boolean running = true;
		DataInputStream din;
		final SendWorker sw;
		
		RecvWorker(Socket sock, Integer nid, SendWorker sw) {
			super("RecvWorker:" + nid);
			this.nid = nid;
			this.sock = sock;
			this.sw = sw;
			try {
				din = new DataInputStream(sock.getInputStream());
				// 等待读取接收到的信息，直到断开连接
				sock.setSoTimeout(0);
			} catch (IOException e) {
				log.error("Error while accessing socket for " + nid, e);
				closeSocket(sock);
				running = false;
			}
		}
		
		@Override
		public void run() {
			threadCnt.incrementAndGet();
			try {
				while (running && !shutdown && sock != null) {
					/**
					 * 读取信息的确定长度
					 */
					if (din.available() > 0) {
						int length = din.readInt();
						if (length <= 0 || length > PACKETMAXSIZE) {
							throw new IOException("Received packet with invalid packet: " + length);
						}
						/**
						 * Allocates a new ByteBuffer to receive the message
						 */
						byte[] msgArray = new byte[length];
						din.readFully(msgArray, 0, length);
						ByteBuffer message = ByteBuffer.wrap(msgArray);
						addToRecvQueue(new Message(message.duplicate(), nid));
					}
				}
			} catch (Exception e) {
				log.warn("Connection broken for id " + nid + ", my id = " + self.getMyId() + ", error = ", e);
			} finally {
				log.warn("Interrupting SendWorker");
				sw.finish();
				if (sock != null) {
					closeSocket(sock);
				}
			}
		}

		synchronized boolean finish() {
			if (!running) {
				return running;
			}
			running = false;

			this.interrupt();
			threadCnt.decrementAndGet();
			return running;
		}
		
	}
	
	/**
	 * 添加信息到接收队列
	 * @author suoyao
	 * @date 下午4:59:37
	 * @param msg
	 */
	public void addToRecvQueue(Message msg) {
		synchronized (recvQLock) {
			if (recvQueue.remainingCapacity() == 0) {
				try {
					recvQueue.remove();
				} catch (NoSuchElementException ne) {
					// element could be removed by poll()
					log.debug("Trying to remove from an empty " + "recvQueue. Ignoring exception " + ne);
				}
			}
			try {
				recvQueue.add(msg);
			} catch (IllegalStateException ie) {
				// This should never happen
				log.error("Unable to insert element in the recvQueue " + ie);
			}
		}
	}
	
	/**
	 * 添加发送信息到发送队列
	 * @author suoyao
	 * @date 下午5:39:24
	 * @param queue
	 * @param buffer
	 */
	private void addToSendQueue(ArrayBlockingQueue<ByteBuffer> queue, ByteBuffer buffer) {
		if (queue.remainingCapacity() == 0) {
			try {
				queue.remove();
			} catch (NoSuchElementException ne) {
				// element could be removed by poll()
				log.debug("Trying to remove from an empty " + "Queue. Ignoring exception " + ne);
			}
		}
		try {
			queue.add(buffer);
		} catch (IllegalStateException ie) {
			// This should never happen
			log.error("Unable to insert an element in the queue " + ie);
		}
	}
	
	private boolean isSendQueueEmpty(ArrayBlockingQueue<ByteBuffer> queue) {
		return queue.isEmpty();
	}
	
	/**
	 *  从发送队列中获取要发送的信息
	 * @author suoyao
	 * @date 下午5:07:43
	 * @param queue
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	private ByteBuffer pollSendQueue(ArrayBlockingQueue<ByteBuffer> queue, long timeout, TimeUnit unit)
	        throws InterruptedException {
		return queue.poll(timeout, unit);
	}
	
	/**
	 * 从 recvQueue头部获取并移除一条信息 如果没有信息，等待一段时间，直到有可用信息为止
	 * @author suoyao
	 * @date 上午10:16:26
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public Message pollRecvQueue(long timeout, TimeUnit unit) throws InterruptedException {
		return recvQueue.poll(timeout, unit);
	}
	
	/**
	 * 检查是否所有的队列都空了，确认所有的信息都已经被交付处理了
	 * @author suoyao
	 * @date 下午3:57:19
	 * @return
	 */
	boolean haveDelivered() {
		for (ArrayBlockingQueue<ByteBuffer> queue : queueSendMap.values()) {
			log.debug("Queue size: " + queue.size());
			if (queue.size() == 0) {
				return true;
			}
		}
		return false;
	}
	
}
