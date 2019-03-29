package org.sam.shen.scheduing.cluster;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
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
	private JobInfoMapper jobInfoMappper;
	
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
			this.rhid = RadishDynamicScheduler.listJobsInScheduler(null).size();
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
	 *  从数据库加载需要调度的job
	 * @author suoyao
	 * @date 下午5:23:24
	 */
	public void loadJobs() {
		// 从数据库加载jobinfo生成任务集合
		List<JobInfo> enableJobInfo = jobInfoMappper.queryLoadedJobs();
		if (null != enableJobInfo && enableJobInfo.size() > 0) {
			enableJobInfo.forEach(jobInfo -> {
				try {
					RadishDynamicScheduler.addJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
				} catch (SchedulerException e) {
					log.error("init add jobInfo failed. {}", jobInfo.getJobName());
				}
			});
		}
	}
	
}
