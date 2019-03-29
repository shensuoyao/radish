package org.sam.shen.scheduing.cluster;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.quartz.SchedulerException;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class FollowerHandler extends Thread {
	protected final Socket sock;
	
	final LeaderNode leader;
	
	// 此为follower节点服务器的nid
	private Integer nid;
	
	/*
	 * 下一个心跳的截止时间，一旦超过这个截止时间，就认为follower不再与leader同步了
	 */
	volatile long tickOfNextAckDeadline;
	
	private DataInputStream is;
	private DataOutputStream os;
	
	// 此packet为死亡数据，如果该packet数据在发送队列中，则表示需要退出发送线程
	final ClusterPacket<?> packetOfDeath = new ClusterPacket<Object>(-1, -1, -1, null);
	
	/*
	 * 此队列是发送给follower的packet信息
	 */
	final LinkedBlockingQueue<ClusterPacket<?>> queuedPackets = new LinkedBlockingQueue<ClusterPacket<?>>();
	
	public FollowerHandler(Socket sock, LeaderNode leader) {
		this.sock = sock;
		this.leader = leader;
		leader.addFollowerHandler(this);
	}

	@Override
	public void run() {
		try {
			tickOfNextAckDeadline = leader.self.tick + leader.self.initLimit + leader.self.syncLimit;
			is =  new DataInputStream(sock.getInputStream());
			os = new DataOutputStream(sock.getOutputStream());
			// 启动发送packet数据包的线程
			new Thread() {
				public void run() {
					Thread.currentThread().setName("Sender-" + sock.getRemoteSocketAddress());
					try {
						sendPackets();
					} catch (InterruptedException e) {
						log.warn("Unexpected interruption", e);
					}
				}
			}.start();
			// 处理接收到的信息
			ClusterPacket<?> cp;
			while(true) {
				cp = new ClusterPacket<>();
				readPacket(cp);
				processPacket(cp);
			}
		} catch (IOException e) {
			if (sock != null && !sock.isClosed()) {
				log.error("Unexpected exception causing shutdown while sock " + "still open", e);
				// close the socket to make sure the
				// other side can see it being close
				try {
					sock.close();
				} catch (IOException ie) {
					// do nothing
				}
			}
		} catch (InterruptedException e) {
			log.error("Unexpected exception causing shutdown", e);
		} finally {
			log.warn("******* GOODBYE " + (sock != null ? sock.getRemoteSocketAddress() : "<null>") + " ********");
			shutdown();
		}
	}
	
	private void sendPackets() throws InterruptedException {
		while (true) {
			try {
				ClusterPacket<?> p;
				p = queuedPackets.poll();
				if (p == null) {
					os.flush();
					p = queuedPackets.take();
				}

				if (p == packetOfDeath) {
					// 关闭线程
					break;
				}
				byte[] bytes = JSON.toJSONBytes(p, SerializerFeature.WriteNullListAsEmpty);
				os.writeInt(bytes.length);
				os.write(bytes);
				os.flush();
				log.info("leader send packet success.");
			} catch (IOException e) {
				if (!sock.isClosed()) {
					log.warn("Unexpected exception at " + this, e);
					try {
						sock.close();
					} catch (IOException ie) {
						log.warn("Error closing socket for handler " + this, ie);
					}
				}
				break;
			}
		}
	}
	
	void readPacket(ClusterPacket<?> cp) throws IOException {
	    if (is.available() > 0) {
            int packetLength = is.readInt();
            byte[] packetBytes = new byte[packetLength];
            if (is.available() >= packetLength) {
                is.readFully(packetBytes, 0, packetLength);
                cp = JSON.parseObject(packetBytes, cp.getClass());
            }
        }
	}
	
	/**
	 *  处理来自follower的同步信息
	 * @author suoyao
	 * @date 下午5:08:02
	 * @param cp
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws SchedulerException 
	 */
	@SuppressWarnings("unchecked") 
	void processPacket(ClusterPacket<?> cp) throws IOException, InterruptedException {
	    if (cp.getType() == null) {
	        return;
        }
		switch (cp.getType()) {
		case LeaderNode.ACK:
			// 确认leader
			this.nid = cp.getNid();
			leader.waitForFollowerAck(this.nid);
			break;
		case LeaderNode.SYNC:
			// follower的同步信息
			Set<Long> jobs = (Set<Long>) cp.getT();
			ClusterPeerNodes.getSingleton().upgradeFollowerSchedulerJobs(cp.getNid(), jobs);
			break;
		case LeaderNode.FOLLOWERINFO:
			// follower的数据信息
			// 主要是follower向leader提交另一个follower的调度同步
			LeaderInfo leaderInfo = (LeaderInfo) cp.getT();
			try {
				// 判断当前需要调度的任务是否为本leader任务
				if(ClusterPeerNodes.getSingleton().getSchedulerJobsView().contains(leaderInfo.getJobId())) {
					// leader自己调度该任务
					// 更新本机的调度服务
                    RadishDynamicScheduler.UpgradeScheduleJob(leaderInfo.getJobId(), leaderInfo.getJobName(), leaderInfo.getCrontab());
				} else {
					ClusterPacket<LeaderInfo> packet = new ClusterPacket<LeaderInfo>(LeaderNode.LEADERINFO,
							leader.self.getMyId(), ClusterPeerNodes.getSingleton().getSchedulerJobCount(), leaderInfo);
					packet.setUxid(cp.getUxid());
					// 加入确认消息队列
					leader.confirmQueue.addConfirmPacket(packet);
					leader.queueFollowerPacket(packet);
				}
				ClusterPacket<Boolean> confirmPacket = new ClusterPacket<>(LeaderNode.COMMIT, leader.self.getMyId(),
				        ClusterPeerNodes.getSingleton().getSchedulerJobCount(), true);
				confirmPacket.setUxid(cp.getUxid());
				queuePacket(confirmPacket);
			} catch (SchedulerException e) {
				ClusterPacket<Boolean> confirmPacket = new ClusterPacket<>(LeaderNode.COMMIT, leader.self.getMyId(),
				        ClusterPeerNodes.getSingleton().getSchedulerJobCount(), false);
				confirmPacket.setUxid(cp.getUxid());
				queuePacket(confirmPacket);
			}
			break;
		case LeaderNode.COMMIT:
			// follower的数据确认信息
			Boolean bool = Boolean.valueOf(cp.getT().toString());
			if(bool) {
				leader.confirmQueue.removeConfirmPacket(cp.getUxid());
			}
			break;
		default:
			break;
		}
	}
	
	public void ping() {
		ClusterPacket<?> ping = new ClusterPacket<String>(LeaderNode.PING, leader.self.getMyId());
		queuePacket(ping);
	}
	
	void queuePacket(ClusterPacket<?> p) {
		queuedPackets.add(p);
	}
	
	public void shutdown() {
		// Send the packet of death
		try {
			queuedPackets.put(packetOfDeath);
		} catch (InterruptedException e) {
			log.warn("Ignoring unexpected exception", e);
		}
		try {
			if (sock != null && !sock.isClosed()) {
				sock.close();
			}
		} catch (IOException e) {
			log.warn("Ignoring unexpected exception during socket close", e);
		}
		this.interrupt();
		leader.removeFollowerHandler(this);
	}
	
	// 是否已经同步
	public boolean acked() {
		// 当前线程状态为存活，并且心跳截止时间大于当前节点的同步次数
		return isAlive() && leader.self.tick <= tickOfNextAckDeadline;
	}
	
}
