package org.sam.shen.scheduing.cluster;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.quartz.SchedulerException;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.sam.shen.scheduing.vo.JobSchedulerVo;

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
	// volatile long tickOfNextAckDeadline;

    // follower同步数据时间
    private long syncDeadline;
	
	private DataInputStream is;
	private DataOutputStream os;
	
	// 此packet为死亡数据，如果该packet数据在发送队列中，则表示需要退出发送线程
	final ClusterPacket<?> packetOfDeath = new ClusterPacket<>(-1, -1, -1, null);
	
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
			// tickOfNextAckDeadline = leader.self.tick + leader.self.initLimit + leader.self.syncLimit;
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
			this.syncDeadline = getSyncDeadLine();
			// 处理接收到的信息
			while(true) {
				ClusterPacket<?> packet = readPacket();
				if (packet != null) {
                    processPacket(packet);
                }
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
				if (p.getType() == LeaderNode.LEADERINFO) {
                    log.info("leader send message: {}", JSON.toJSONString(p, SerializerFeature.WriteNullListAsEmpty));
                }
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

    private ClusterPacket<?> readPacket() throws IOException {
	    if (is.available() > 0) {
            int packetLength = is.readInt();
            byte[] packetBytes = new byte[packetLength];
            if (is.available() >= packetLength) {
                is.readFully(packetBytes, 0, packetLength);
                return JSON.parseObject(packetBytes, ClusterPacket.class);
            }
        }
        return null;
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
	private void processPacket(ClusterPacket<?> cp) throws IOException, InterruptedException {
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
				List<Long> ts = JSONArray.parseArray(JSON.toJSONString(cp.getT()), Long.class);
				Set<Long> jobs = new HashSet<>(ts);
				ClusterPeerNodes.getSingleton().upgradeFollowerSchedulerJobs(cp.getNid(), jobs);
				// 刷新同步超时时间点
				this.syncDeadline = getSyncDeadLine();
				break;
			case LeaderNode.FOLLOWERINFO:
				// follower的数据信息
				// 主要是follower向leader提交另一个follower的调度同步
				LeaderInfo leaderInfo = JSON.toJavaObject((JSONObject) cp.getT(), LeaderInfo.class);
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
			case LeaderNode.LOADED:
			    // 确认follower节点任务已经加载完毕
                List<JobSchedulerVo> errorJobs = JSONArray.parseArray(JSON.toJSONString(cp.getT()), JobSchedulerVo.class);
                leader.loadPacket.addErrorJobs(errorJobs);

                leader.loadPacket.removeLoadedJobs(cp.getNid());
				break;
            case LeaderNode.CLUSTERSERVER:
                ClusterPeer.ClusterServer server = JSON.parseObject(JSON.toJSONString(cp.getT()), ClusterPeer.ClusterServer.class);
                // 如果新加入的节点不在cluster中，则添加该节点，并广播给其他从节点
                if (!leader.self.getClusterServers().containsKey(server.nid)) {
                    // 给当前节点添加ClusterServer
                    leader.self.getClusterServers().put(server.nid, server);
                    // 广播给其他子节点
                    ClusterPacket<ClusterPeer.ClusterServer> packet = new ClusterPacket<>(LeaderNode.CLUSTERSERVER,
                            leader.self.getMyId(), ClusterPeerNodes.getSingleton().getSchedulerJobCount(), server);
                    leader.broadcastPacket(packet);
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

	private long getSyncDeadLine() {
	    return System.currentTimeMillis() + leader.self.tickTime * 20;
    }
	
	// 是否已经同步
	public boolean synced() {
		// 当前线程状态为存活，并且心跳截止时间大于当前节点的同步次数
		return isAlive() && System.currentTimeMillis() < this.syncDeadline;
	}

}
