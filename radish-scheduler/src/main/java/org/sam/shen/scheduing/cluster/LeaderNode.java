package org.sam.shen.scheduing.cluster;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.sam.shen.scheduing.entity.JobScheduler;
import org.sam.shen.scheduing.vo.JobSchedulerVo;

/**
 * leader节点
 * @author suoyao
 * @date 2019年3月8日 下午4:29:04
  * 
 */
@Slf4j
public class LeaderNode {

	final ClusterPeer self;
	private ServerSocket ss;
	
	private boolean isShutdown;
	
	/*
	 * follower和leader的确认标志
	 *  确认leader和follower关系
	 *  并将follower调度的任同步给leader
	 */
	final static int ACK = 1;
	
	/*
	 * follower与leader的心跳
	 * 并通过心跳将follower的调度任务列表发送给leader
	 */
	final static int PING = 2;
	
	// follower与leader同步调度任务表的标志
	final static int SYNC = 3;
	
	// leader向follower发送调度任务更新信息的标志
	final static int LEADERINFO = 4;
	
	// follower向leader发送调度任务更新信息的标志
	final static int FOLLOWERINFO = 5;
	
	/*
	 *  事务提交确认标志
	 *  follower向leader确认事务已经被执行
	 */
	final static int COMMIT = 6;

	// leader发送给follower初始加载任务的标志
	final static int LOAD = 7;

	// follower发送给leader任务已加载的标志
	final static int LOADED = 8;

	// 用于同步ClusterServer信息
	final static int CLUSTERSERVER = 9;
	
	private FollowerCnxAcceptor cnxAcceptor;
	
	private final HashSet<FollowerHandler> followers = new HashSet<>();
	
	protected ConfirmPacketQueue confirmQueue;

	protected LoadPacket loadPacket;
	
	// 返回当前follower的副本快照
	public List<FollowerHandler> getFollowers() {
        synchronized (followers) {
            return new ArrayList<>(followers);
        }
    }
	
	void addFollowerHandler(FollowerHandler follower) {
        synchronized (followers) {
            followers.add(follower);
        }
    }
	
	public LeaderNode(ClusterPeer clusterPeer) throws IOException {
		this.confirmQueue = new ConfirmPacketQueue();
		this.loadPacket = new LoadPacket();
		this.self = clusterPeer;
		try {
			if (self.isClusterListenOnAllIPs()) {
				ss = new ServerSocket(self.getMyLeaderAddr().getPort());
			} else {
				ss = new ServerSocket();
			}
			ss.setReuseAddress(true);
			if (!self.isClusterListenOnAllIPs()) {
				ss.bind(self.getMyLeaderAddr());
			}
		} catch (IOException e) {
			if (self.isClusterListenOnAllIPs()) {
				log.error("Couldn't bind to port " + self.getMyLeaderAddr().getPort(), e);
			} else {
				log.error("Couldn't bind to " + self.getMyLeaderAddr(), e);
			}
			throw e;
		}
	}
	
	class FollowerCnxAcceptor extends Thread {
		private volatile boolean stop = false;

		@Override
		public void run() {
			try {
				while (!stop) {
					try {
						Socket s = ss.accept();
						// start with the initLimit, once the ack is processed
						// in FollowerHandler switch to the syncLimit
						s.setSoTimeout(self.tickTime * self.initLimit);
						s.setTcpNoDelay(false);
						FollowerHandler fh = new FollowerHandler(s, LeaderNode.this);
						fh.start();
					} catch (SocketException e) {
						if (stop) {
							log.info("exception while shutting down acceptor: " + e);

							// When Leader.shutdown() calls ss.close(),
							// the call to accept throws an exception.
							// We catch and set stop to true.
							stop = true;
						} else {
							throw e;
						}
					}
				}
			} catch (Exception e) {
				log.warn("Exception while accepting follower", e);
			}
		}

		public void halt() {
			stop = true;
		}
	}
	
	void lead() throws InterruptedException {
		self.end_fle = System.currentTimeMillis();
		log.info("LEADING - LEADER ELECTION TOOK - " + (self.end_fle - self.start_fle));
		self.start_fle = 0;
		self.end_fle = 0;
		
		try {
			// 开启follower连接监听, 接收follower的连接
			cnxAcceptor = new FollowerCnxAcceptor();
			cnxAcceptor.start();
			
			// 启动确认数据包队列发送线程
			new Thread(() -> {
                Thread.currentThread().setName("Leader-" + ss.getLocalSocketAddress());
                try {
                    while (true) {
                        ClusterPacket<LeaderInfo> confirmPacket = confirmQueue.pollConfirmPacket();
                        if (null == confirmPacket) {
                            confirmPacket = confirmQueue.takeConfirmPacket();
                        }
                        queueFollowerPacket(confirmPacket);
                        // leaderBufferOs.write(JSON.toJSONBytes(confirmPacket, SerializerFeature.WriteNullListAsEmpty));
                    }
                } catch (InterruptedException e) {
                    log.warn("Unexpected interruption", e);
                }
            }).start();
			
			/*
			 * 调用该方法是为了让当前线程等待
			* 等待超过一般的follower确认ACK之后开始建立心跳
			 */
			waitForFollowerAck(self.getMyId());

			new Thread(() -> {
			    Thread.currentThread().setName("Leader-Load Scheduler");
                // 加载需要调度的任务
                List<JobSchedulerVo> total = self.loadJobsFirst();
                // 如果任务没有分配完成，则重新分配
                while (!loadPacket.isEmpty()) {
                    List<JobSchedulerVo> jobs = new ArrayList<>();
                    for (List<JobSchedulerVo> l : loadPacket.getToLoadJobMap().values()) {
                        jobs.addAll(l);
                    }
                    for (List<JobSchedulerVo> l : loadPacket.getLoadingJobMap().values()) {
                        jobs.addAll(l);
                    }
                    List<Integer> nidArr = new ArrayList<>(self.getClusterServers().keySet());
                    nidArr.removeAll(loadPacket.getToLoadJobMap().keySet());
                    nidArr.removeAll(loadPacket.getLoadingJobMap().keySet());
                    loadPacket.clearLoadMap();
                    self.loadJobs(jobs, nidArr);
                }
                List<JobSchedulerVo> errors = loadPacket.getErrorJobs();
                List<Long> errorIds = errors.stream().map(JobScheduler::getJobId).collect(Collectors.toList());
                List<Long> successIds = total.stream().filter(job -> !errorIds.contains(job.getJobId()))
                        .map(JobSchedulerVo::getJobId).collect(Collectors.toList());
                log.info("Load result: {} schedulers{} have loaded, {} schedulers{} have failed",
                        successIds.size(), successIds.toString(), errorIds.size(), errorIds.toString());
            }).start();


			// 向follower发送心跳
			while (true) {
				Thread.sleep(self.tickTime);

                // 检查是否有超过半数的从节点已发送同步数据
                Set<Integer> syncedSet = new HashSet<>();
                for (FollowerHandler f : getFollowers()) {
                    if (f.synced()) {
                        syncedSet.add(f.getNid());
                    }
                }
                int half = followers.size() / 2;
                if (syncedSet.size() <= half) {
                    return;
                }

                // 像follower节点发送心跳
				for (FollowerHandler f : getFollowers()) {
					f.ping();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private HashSet<Integer> electingFollowers = new HashSet<>();
	private boolean electionFinished = false;

	/**
	 * 等待follower的ack确认
	 * @author suoyao
	 * @date 下午3:16:56
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void waitForFollowerAck(int nid) throws IOException, InterruptedException {
		synchronized (electingFollowers) {
			if (electionFinished) {
				return;
			}
			if(self.getMyId() != nid) {
				// 不能把自己加入到followers列表中
				electingFollowers.add(nid);
			}
			int half = self.getClusterServers().size() / 2;
			if (!electingFollowers.contains(self.getMyId()) && electingFollowers.size() >= half) {
				electionFinished = true;
				electingFollowers.notifyAll();
			} else {
				long start = System.currentTimeMillis();
				long cur = start;
				long end = start + self.getInitLimit() * self.getTickTime();
				while (!electionFinished && cur < end) {
					electingFollowers.wait(end - cur);
					cur = System.currentTimeMillis();
				}
				if (!electionFinished) {
					throw new InterruptedException("Timeout while waiting for follower acked");
				}
			}
		}
	}
	
	void removeFollowerHandler(FollowerHandler follower) {
		synchronized (followers) {
			followers.remove(follower);
			// 当有一个从节点挂掉之后需要将从节点运行的任务重新分配到当前运行中的节点
            ClusterPeerNodes.getSingleton().removeFollowerSchedulerJobs(follower.getNid());
            new Thread(() -> self.loadFollowerJobs(follower.getNid())).start();
		}
	}
	
	void shutdown(String reason) {
		log.info("Shutting down {}", reason);
		if (isShutdown) {
			return;
		}
		log.info("Shutdown called", new Exception("shutdown Leader! reason: " + reason));

		if (cnxAcceptor != null) {
			cnxAcceptor.halt();
		}
		try {
			ss.close();
		} catch (IOException e) {
			log.warn("Ignoring unexpected exception during close", e);
		}
		synchronized (followers) {
			for (Iterator<FollowerHandler> it = followers.iterator(); it.hasNext();) {
				FollowerHandler f = it.next();
				it.remove();
				f.shutdown();
			}
		}
		isShutdown = true;
	}
	
	public void queueFollowerPacket(ClusterPacket<LeaderInfo> packet) {
		// 寻找当前job所在的节点服务器
		Integer targetNid = ClusterPeerNodes.getSingleton().findFollowerNidByJob(packet.getT().getJobId());
		for (FollowerHandler f : getFollowers()) {
			if(f.getNid().equals(targetNid)) {
				packet.setType(LeaderNode.LEADERINFO);
				packet.setNid(self.getMyId());
				packet.setRhid(ClusterPeerNodes.getSingleton().getSchedulerJobCount());
				f.queuePacket(packet);
			}
		}
	}

    /**
     * 广播通信数据包
     * @param packet 通信包
     */
	public void broadcastPacket(ClusterPacket<?> packet) {
	    for (FollowerHandler f : getFollowers()) {
	        if (!f.getNid().equals(self.getMyId())) {
	            f.queuePacket(packet);
            }
        }
    }
	
}
