package org.sam.shen.scheduing.cluster;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.mapper.JobSchedulerMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.sam.shen.scheduing.vo.JobSchedulerVo;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 集群观察者
 * @author suoyao
 * @date 2019年2月28日 下午5:55:32
  * 
 */
@Getter
@Setter
@Slf4j
@Component
public class ClusterPeer extends Thread {
	
	@Resource
	private JobInfoMapper jobInfoMapper;

	@Resource
	private JobSchedulerMapper jobSchedulerMapper;
	
	// 当前节点服务器的node ID
	private Integer myId;
	
	protected int initLimit;
	
	// 滚动时间
	protected int tickTime;
	
	// 同步超时时间，毫秒为单位
	protected int syncLimit;
	
	// cnx管理连接的超时 毫秒 时间
	protected int cnxTimeout = -1;
	
	protected boolean clusterListenOnAllIPs = false;
	
	// 集群中的节点服务器
	protected Map<Integer, ClusterServer> clusterServers;
	
	// 当前节点状态
	private NodeState nodeState = NodeState.LOOKING;
	
	volatile boolean running = true;
	
	// 当前投票节点
	volatile private Vote currentVote;
	
	// 自己的服务地址
	private InetSocketAddress myLeaderAddr;
	
	Election electionAlg;
	
	ClusterCnxManager ccm;
	
	public LeaderNode leaderNode;
	public FollowerNode followerNode;
	
	// 选举开始时间与leader，follower时间
	public long start_fle, end_fle;
	
	// 心跳次数
	// protected volatile long tick;
	
	synchronized protected void setLeaderNode(LeaderNode leaderNode) {
		this.leaderNode = leaderNode;
	}
	
	synchronized protected void setFollowerNode(FollowerNode followerNode) {
		this.followerNode = followerNode;
	}

	public synchronized NodeState getNodeState() {
		return nodeState;
	}
	
	public synchronized void setCurrentVote(Vote v) {
		currentVote = v;
	}
	
	public synchronized void setPeerState(NodeState newState) {
		nodeState = newState;
	}
	
	@Override
	public void run() {
		setName("ClusterPeer" + "[myid=" + getMyId() + "]");
		log.info("Starting cluster peer.");
		
		// 执行集群间服务
		while(running) {
			switch (getNodeState()) {
			case LOOKING:
				log.info("Cluster peer LOOKING.");
				try {
					setCurrentVote(this.electionAlg.lookForLeader());
				} catch (InterruptedException e) {
					log.warn("Unexpected exception", e);
					setPeerState(NodeState.LOOKING);
				}
				break;
			case FOLLOWING:
				try {
					log.info("Cluster peer FOLLOWING.");
					setFollowerNode(new FollowerNode(this));
					followerNode.follower();
				} catch (Exception e) {
					log.warn("Unexpected exception", e);
				} finally {
					followerNode.shutdown();
					// 取消ClusterPeer对FollowerNode的循环依赖
					setFollowerNode(null);
					setPeerState(NodeState.LOOKING);
				}
				break;
			case LEADING:
				log.info("Cluster peer LEADING.");
				try {
					setLeaderNode(new LeaderNode(this));
					leaderNode.lead();
					// 取消ClusterPeer对LeaderNode的循环依赖
					setLeaderNode(null);
				} catch (IOException e) {
					log.warn("Unexpected exception",e);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					if (leaderNode != null) {
						leaderNode.shutdown("Forcing shutdown");
						setLeaderNode(null);
					}
					setPeerState(NodeState.LOOKING);
				}
				break;
			case OBSERVING:
				log.info("No State with OBSERVING.");
				break;
			}
		}
	}

	@Override
	public synchronized void start() {
		startLeaderElection();
		super.start();
	}

	/**
	 * 集群服务器
	 * @author suoyao
	 * @date 2019年3月1日 下午2:44:18
	  * 
	 */
	public static class ClusterServer {
		
		public ClusterServer(int nid, InetSocketAddress addr) {
			this.nid = nid;
			this.addr = addr;
		}
		
		public ClusterServer(int nid, InetSocketAddress addr, InetSocketAddress electionAddr) {
			this.nid = nid;
			this.addr = addr;
			this.electionAddr = electionAddr;
		}
		
		// leader 地址
		public InetSocketAddress addr;

		// 选举地址
		public InetSocketAddress electionAddr;

		public int nid;
	}
	
	public Map<Integer, ClusterPeer.ClusterServer> getView() {
		return Collections.unmodifiableMap(this.clusterServers);
	}
	
	/**
	 * 投票节点服务器视图
	 * @author suoyao
	 * @date 下午4:14:14
	 * @return
	 */
	public Map<Integer, ClusterPeer.ClusterServer> getVotingView() {
		Map<Integer, ClusterPeer.ClusterServer> ret = new HashMap<Integer, ClusterPeer.ClusterServer>();
		Map<Integer, ClusterPeer.ClusterServer> view = getView();
		for (ClusterServer cs : view.values()) {
			if (cs.nid != this.getMyId()) {
				ret.put(cs.nid, cs);
			}
		}
		return ret;
	}
	
	/**
	 * 节点状态
	 * LOOKING 表示开始寻找leader
	 * FOLLOWING 表示当前集群中存在leader，节点处在更随状态
	 * LEADING 表示当前节点为leader节点
	 * OBSERVING 表示当前节点为观察者，并未准备好加入集群
	 * @author suoyao
	 * @date 2019年3月1日 下午3:25:34
	  * 
	 */
	public enum NodeState {
		LOOKING, FOLLOWING, LEADING, OBSERVING;
	}
	
	private int rhid;
	private long electionEpoch;
	
	/**
	 * 更新正在调度的任务数量
	 * @author suoyao
	 * @date 下午6:06:21
	 * @return
	 */
	public int updateRhid() {
		try {
			this.rhid = RadishDynamicScheduler.listJobsInScheduler().size();
		} catch (SchedulerException e) {
			log.error("get scheduler jobs error.", e);
		}
		return rhid;
	}
	
	/**
	 * 开始选举
	 * @author suoyao
	 * @date 下午3:11:01
	 */
	synchronized public void startLeaderElection() {
		currentVote = new Vote(myId, getRhid());
		// 设置本机地址
		for(ClusterServer s : getView().values()) {
			if(s.nid == myId) {
				// 设置本机的leader同步地址
				myLeaderAddr = s.addr;
				break;
			}
		}
		if (myLeaderAddr == null) {
			log.error("My id " + myId + " not in the node list");
			throw new RuntimeException("My id " + myId + " not in the node list");
		}
		// 初始化网络连接管理器
		ccm = new ClusterCnxManager(this);
		// 初始化启动选举监听
		ClusterCnxManager.Listener listener = ccm.listener;
		if(null != listener) {
			listener.start();
			 this.electionAlg = new FastLeaderElection(this, ccm);
		}
	}

	/**
	 * 从数据库加载需要调度的job
	 * @author suoyao
	 * @date 下午5:23:24
	 */
	public void loadJobs(List<JobSchedulerVo> jobs, List<Integer> nids) {
		// 从数据库加载jobinfo生成任务集合
		if (null != jobs && jobs.size() > 0) {
            // 将待加载的任务平均分配到分布式集群中
            Map<Integer, List<JobSchedulerVo>> jobMap = new HashMap<>();
            nids.remove(myId);
            nids.add(0, myId);
            for (int i = 0; i < jobs.size(); i++) {
                Integer nid = nids.get(i % nids.size());
                jobMap.computeIfAbsent(nid, k -> new ArrayList<>());
                jobMap.get(nid).add(jobs.get(i));
            }
            // 给当前节点加载调度任务
            List<JobSchedulerVo> currentJobs = jobMap.get(myId);
            for (JobSchedulerVo job : currentJobs) {
                try {
                    RadishDynamicScheduler.addJob(job.getJobId(), job.getJobName(), job.getCrontab());
                } catch (Exception e) {
                    leaderNode.loadPacket.addErrorJob(job);
                }
            }
            jobMap.remove(myId);
            // 分配给其他节点加载任务
            leaderNode.loadPacket.setToLoadJobMap(jobMap);
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime <= tickTime * 5 && !leaderNode.loadPacket.isEmpty()) {
                for (FollowerHandler f : leaderNode.getFollowers()) { // 循环防止有些follower还没建立连接
                    List<JobSchedulerVo> loadJobs = leaderNode.loadPacket.getToLoadJobs(f.getNid());
                    if (loadJobs != null && loadJobs.size() > 0) {
                        ClusterPacket<List<JobSchedulerVo>> clusterPacket = new ClusterPacket<>(LeaderNode.LOAD, myId, rhid, loadJobs);
                        f.queuePacket(clusterPacket);
                        leaderNode.loadPacket.loadJob(f.getNid());
                    }
                }
            }
		}
	}

    /**
     * 第一次加载全部调度任务
     * @author clock
     * @date 2019/4/11 上午10:18
     * @return 所有待加载的调度任务
     */
	public List<JobSchedulerVo> loadJobsFirst() {
        List<JobSchedulerVo> jobs = jobSchedulerMapper.queryAllScheduler();
        List<Integer> nidArr = new ArrayList<>(clusterServers.keySet());
        loadJobs(jobs, nidArr);
        return jobs;
    }

    public void loadFollowerJobs(Integer nid) {
        leaderNode.loadPacket.clearAll();
	    List<JobSchedulerVo> followerJobs = jobSchedulerMapper.querySchedulerByNid(nid);
        List<Integer> nids = leaderNode.getFollowers().stream().map(FollowerHandler::getNid).collect(Collectors.toList());
        nids.add(0, myId);
        do {
            leaderNode.loadPacket.clearLoadMap();
            loadJobs(followerJobs, nids);
            List<JobSchedulerVo> jobs = new ArrayList<>();
            for (List<JobSchedulerVo> l : leaderNode.loadPacket.getToLoadJobMap().values()) {
                jobs.addAll(l);
            }
            for (List<JobSchedulerVo> l : leaderNode.loadPacket.getLoadingJobMap().values()) {
                jobs.addAll(l);
            }
            List<Integer> nidArr = new ArrayList<>(this.clusterServers.keySet());
            nidArr.removeAll(leaderNode.loadPacket.getToLoadJobMap().keySet());
            nidArr.removeAll(leaderNode.loadPacket.getLoadingJobMap().keySet());
            followerJobs = jobs;
            nids = nidArr;
        } while (!leaderNode.loadPacket.isEmpty());
    }
	
}
