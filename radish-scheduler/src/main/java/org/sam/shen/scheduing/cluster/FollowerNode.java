package org.sam.shen.scheduing.cluster;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.quartz.SchedulerException;
import org.sam.shen.scheduing.cluster.ClusterPeer.ClusterServer;
import org.sam.shen.scheduing.cluster.utils.ByteUtils;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.extern.slf4j.Slf4j;
import org.sam.shen.scheduing.vo.JobSchedulerVo;

/**
 * follower 节点
 * @author suoyao
 * @date 2019年3月8日 下午4:29:14
  * 
 */
@Slf4j
public class FollowerNode {

	private final ClusterPeer self;
    private DataInputStream leaderIs;
    private DataOutputStream leaderBufferOs;

    private Socket sock;
    private ConfirmPacketQueue confirmQueue;

    private long beatTime;
	
	FollowerNode(ClusterPeer clusterPeer) {
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
			// 确认关系后向leader节点发送ClusterServer信息
            sendClusterServer();
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
						Thread.currentThread().interrupt();
					} catch (IOException e) {
						log.warn("Unexpected IOException", e);
					}
				}
			}.start();

			this.beatTime = System.currentTimeMillis();
			while (self.isRunning()) {
			    // check whether server socket is closed
                if (System.currentTimeMillis() - beatTime > self.tickTime * 20) {
                    throw new IOException("Heart beat of leader is timeout");
                }
				ClusterPacket<?> packet = readPacket();
				if (packet != null) {
                    processPacket(packet);
                }
			}
		} catch (IOException e) {
			log.warn("IOException when following the leader", e);
		} catch (InterruptedException e) {
			log.warn("InterruptedException when following the leader", e);
			Thread.currentThread().interrupt();
		} finally {
			try {
				sock.close();
			} catch (IOException e1) {
				log.error("error:", e1);
			}
		}
	}
	
	private InetSocketAddress findLeader() {
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
	private void connectToLeader(InetSocketAddress addr) throws IOException, ConnectException, InterruptedException {
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
	
	private void writePacket(ClusterPacket<?> cp, boolean flush) throws IOException {
		synchronized (leaderBufferOs) {
			if (cp != null) {
                byte[] body = JSON.toJSONBytes(cp, SerializerFeature.WriteNullListAsEmpty);
                byte[] header = ByteUtils.intToByteArray(body.length);
                byte[] bytes = ByteUtils.mergeByteArray(header, body);
                leaderBufferOs.write(bytes);
                leaderBufferOs.flush();
                if (cp.getType() == LeaderNode.FOLLOWERINFO) {
                    log.info("follower send message: {}", JSON.toJSONString(cp, SerializerFeature.WriteNullListAsEmpty));
                }
			}
			if (flush) {
				leaderBufferOs.flush();
			}
		}
	}

    private ClusterPacket<?> readPacket() throws IOException {
		synchronized (leaderIs) {
//            if (leaderIs.available() >= 4) { // header存储每个数据包的长度，为4个字节，确保能读出来
                byte[] header = new byte[4];
                leaderIs.readFully(header, 0, header.length);
                int bodyLength = ByteUtils.byteArrayToInt(header);

                byte[] body = new byte[bodyLength];
//                while (leaderIs.available() < bodyLength);
                leaderIs.readFully(body, 0, bodyLength);
                return JSON.parseObject(body, ClusterPacket.class);
//            }
		}
//		return null;
	}
	
	private void ackWithLeader() throws IOException {
		ClusterPacket<String> packet = new ClusterPacket<>(LeaderNode.ACK, self.getMyId());
		writePacket(packet, true);
	}

	private void sendClusterServer() throws IOException {
	    ClusterPacket<ClusterServer> packet = new ClusterPacket<>(LeaderNode.CLUSTERSERVER, self.getMyId(),
                ClusterPeerNodes.getSingleton().getSchedulerJobCount(),
                self.getClusterServers().get(self.getMyId()));
	    writePacket(packet, true);
    }

    private void syncWithLeader() throws IOException {
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

    private void processPacket(ClusterPacket<?> cp) throws IOException {
	    if (cp.getType() == null) {
	        return;
        }
		switch (cp.getType()) {
		case LeaderNode.PING:
			syncWithLeader();
            this.beatTime = System.currentTimeMillis();
			break;
		case LeaderNode.LEADERINFO:
			try {
				// 处理leader的信息
				LeaderInfo leaderInfo = JSON.toJavaObject((JSONObject) cp.getT(), LeaderInfo.class);
				List<Long> jobIds = ClusterPeerNodes.getSingleton().getSchedulerJobsView();
				boolean ret;
				if(jobIds.contains(leaderInfo.getJobId())) {
				    // 如果调度任务可用则更新
                    ret = RadishDynamicScheduler.UpgradeScheduleJob(leaderInfo.getJobId(), leaderInfo.getCreateTime(), leaderInfo.getCrontab());
				} else {
					// 新增调度job
					ret = RadishDynamicScheduler.addJob(leaderInfo.getJobId(), leaderInfo.getCreateTime(), leaderInfo.getCrontab());
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
		case LeaderNode.LOAD:
		    // 加载leader节点分配的任务
		    List<JobSchedulerVo> jobs = JSONArray.parseArray(JSON.toJSONString(cp.getT()), JobSchedulerVo.class);
		    List<JobSchedulerVo> errorJobs = new ArrayList<>();
		    for (JobSchedulerVo job : jobs) {
		        try {
		            RadishDynamicScheduler.addJob(job.getJobId(), job.getCreateTime().getTime(), job.getCrontab());
                } catch (Exception e) {
		            errorJobs.add(job);
                }
            }
            ClusterPacket<List<JobSchedulerVo>> loadedPacket = new ClusterPacket<>(LeaderNode.LOADED, self.getMyId(), self.getRhid(), errorJobs);
		    writePacket(loadedPacket, true);
		    break;
        case LeaderNode.CLUSTERSERVER:
            ClusterPeer.ClusterServer server = JSON.parseObject(JSON.toJSONString(cp.getT()), ClusterPeer.ClusterServer.class);
            // 如果新加入的节点不在cluster中，则添加该节点
            if (!self.getClusterServers().containsKey(server.nid)) {
                // 给当前节点添加ClusterServer
                self.getClusterServers().put(server.nid, server);
            }
            break;
		default:
		}
	}
	
}
