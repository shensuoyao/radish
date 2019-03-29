package org.sam.shen.scheduing.cluster;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import org.quartz.SchedulerException;
import org.sam.shen.scheduing.cluster.ClusterPeer.ClusterServer;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * follower 节点
 * @author suoyao
 * @date 2019年3月8日 下午4:29:14
  * 
 */
@Slf4j
public class FollowerNode {

	final ClusterPeer self;
	protected DataInputStream leaderIs;
	protected DataOutputStream leaderBufferOs;

	protected Socket sock;
	protected ConfirmPacketQueue confirmQueue;
	
	public FollowerNode(ClusterPeer clusterPeer) {
		this.self = clusterPeer;
		this.confirmQueue = new ConfirmPacketQueue();
	}
	
	void follower() {
		self.end_fle = System.currentTimeMillis();
		log.info("FOLLOWING - LEADER ELECTION TOOK - " + (self.end_fle - self.start_fle));
		self.start_fle = 0;
		self.end_fle = 0;
		try {
			InetSocketAddress addr = findLeader();
			connectToLeader(addr);
			// 与leader服务器确认follower关系
			ackWithLeader();
			// 启动确认数据包队列发送线程
			new Thread() {
				public void run() {
					Thread.currentThread().setName("Sender-" + sock.getRemoteSocketAddress());
					try {
						while(true) {
							ClusterPacket<LeaderInfo> confirmPacket = confirmQueue.pollConfirmPacket();
							if(null == confirmPacket) {
								confirmPacket = confirmQueue.takeConfirmPacket();
							}
							writePacket(confirmPacket, true);
						}
					} catch (InterruptedException e) {
						log.warn("Unexpected interruption", e);
					} catch (IOException e) {
						log.warn("Unexpected IOException", e);
					}
				}
			}.start();
			
			while (self.isRunning()) {
				ClusterPacket<?> packet = readPacket();
				processPacket(packet);
			}
		} catch (IOException | InterruptedException e) {
			log.warn("Exception when following the leader", e);
			try {
				sock.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	protected InetSocketAddress findLeader() {
		InetSocketAddress addr = null;
		// Find the leader by nid
		Vote current = self.getCurrentVote();
		for (ClusterServer s : self.getView().values()) {
			if (s.nid == current.getNid()) {
				addr = s.addr;
				break;
			}
		}
		if (addr == null) {
			log.warn("Couldn't find the leader with id = " + current.getNid());
		}
		return addr;
	}
	
	/**
	 * 尝试连接到leader服务器，
	 * 如果连接失败，则重试5次后放弃
	 * @author suoyao
	 * @date 上午11:10:57
	 * @param addr
	 * @throws IOException
	 * @throws ConnectException
	 * @throws InterruptedException
	 */
	protected void connectToLeader(InetSocketAddress addr) throws IOException, ConnectException, InterruptedException {
		sock = new Socket();
		sock.setSoTimeout(self.tickTime * self.initLimit);
		for (int tries = 0; tries < 5; tries++) {
			try {
				sock.connect(addr, self.tickTime * self.syncLimit);
				break;
			} catch (IOException e) {
				if (tries == 4) {
					log.error("Unexpected exception", e);
					throw e;
				} else {
					log.warn("Unexpected exception, tries=" + tries + ", connecting to " + addr, e);
					sock = new Socket();
					sock.setSoTimeout(self.tickTime * self.initLimit);
				}
			}
			Thread.sleep(1000);
		}
		leaderIs = new DataInputStream(sock.getInputStream());
		leaderBufferOs = new DataOutputStream(sock.getOutputStream());
	}
	
	public void shutdown() {
		log.info("shutdown called", new Exception("shutdown Follower"));
	}
	
	void writePacket(ClusterPacket<?> cp, boolean flush) throws IOException {
		synchronized (leaderBufferOs) {
			if (cp != null) {
                byte[] bytes = JSON.toJSONBytes(cp, SerializerFeature.WriteNullListAsEmpty);
                leaderBufferOs.writeInt(bytes.length);
                leaderBufferOs.write(bytes);
                leaderBufferOs.flush();
                log.info("send follower packet success.");
			}
			if (flush) {
				leaderBufferOs.flush();
			}
		}
	}

    ClusterPacket<?> readPacket() throws IOException {
		synchronized (leaderIs) {
            if (leaderIs.available() > 0) {
                int packetLength = leaderIs.readInt();
                byte[] packetBytes = new byte[packetLength];
                leaderIs.readFully(packetBytes, 0, packetLength);
                return JSON.parseObject(packetBytes, ClusterPacket.class);
            }
		}
		return null;
	}
	
	public void ackWithLeader() throws IOException {
		ClusterPacket<String> packet = new ClusterPacket<String>(LeaderNode.ACK, self.getMyId());
		writePacket(packet, true);
	}
	
	public void syncWithLeader() throws IOException {
		ClusterPacket<List<Long>> packet = new ClusterPacket<>(LeaderNode.SYNC, self.getMyId(),
		        ClusterPeerNodes.getSingleton().getSchedulerJobCount());
		packet.setT(ClusterPeerNodes.getSingleton().getSchedulerJobsView());
		writePacket(packet, true);
	}

	public void sendFollowerInfo(LeaderInfo leaderInfo) throws IOException {
        ClusterPacket<LeaderInfo> packet = new ClusterPacket<>(LeaderNode.FOLLOWERINFO, self.getMyId(),
                ClusterPeerNodes.getSingleton().getSchedulerJobCount());
        packet.setT(leaderInfo);
        writePacket(packet, true);
    }
	
	protected void processPacket(ClusterPacket<?> cp) throws IOException {
	    if (cp.getType() == null) {
	        return;
        }
		switch (cp.getType()) {
		case LeaderNode.PING:
			syncWithLeader();
			break;
		case LeaderNode.LEADERINFO:
			try {
				// 处理leader的信息
				LeaderInfo leaderInfo = (LeaderInfo) cp.getT();
				List<Long> jobIds = ClusterPeerNodes.getSingleton().getSchedulerJobsView();
				boolean ret;
				if(jobIds.contains(leaderInfo.getJobId())) {
				    // 如果调度任务可用则更新
                    ret = RadishDynamicScheduler.UpgradeScheduleJob(leaderInfo.getJobId(), leaderInfo.getJobName(), leaderInfo.getCrontab());
				} else {
					// 新增调度job
					ret = RadishDynamicScheduler.addJob(leaderInfo.getJobId(), leaderInfo.getJobName(), leaderInfo.getCrontab());
				}
				// 发送COMMIT 确认信息
				ClusterPacket<Boolean> commitPacket = new ClusterPacket<Boolean>(LeaderNode.COMMIT, self.getMyId(),
						ClusterPeerNodes.getSingleton().getSchedulerJobCount(), ret);
				writePacket(commitPacket, true);
			} catch (SchedulerException e) {
				ClusterPacket<Boolean> commitPacket = new ClusterPacket<Boolean>(LeaderNode.COMMIT, self.getMyId(),
						ClusterPeerNodes.getSingleton().getSchedulerJobCount(), false);
				writePacket(commitPacket, true);
			}
			break;
		case LeaderNode.COMMIT:
			Boolean bool = Boolean.valueOf(cp.getT().toString());
			if(bool) {
				confirmQueue.removeConfirmPacket(cp.getUxid());
			}
			break;
		default:
		}
	}
	
}
