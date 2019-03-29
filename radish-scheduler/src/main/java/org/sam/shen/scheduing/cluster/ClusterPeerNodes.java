package org.sam.shen.scheduing.cluster;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * 集群节点数据
 * @author suoyao
 * @date 2019年3月13日 上午11:43:56
 * 
 */
@Slf4j
public class ClusterPeerNodes {
	private static volatile ClusterPeerNodes singleton = null;

	private ClusterPeerNodes() {
	}

	public static ClusterPeerNodes getSingleton() {
		if (singleton == null) {
			synchronized (ClusterPeerNodes.class) {
				if (singleton == null) {
					singleton = new ClusterPeerNodes();
				}
			}
		}
		return singleton;
	}

	/*
	 * 存放当前节点服务器调度的所有任务
	 */
	private CopyOnWriteArrayList<Long> nodeSchedulerJobs = Lists.newCopyOnWriteArrayList();
	
	public int getSchedulerJobCount() {
		return nodeSchedulerJobs.size();
	}
	
	public List<Long> getSchedulerJobsView() {
		return Collections.unmodifiableList(nodeSchedulerJobs);
	}

	public void addSchedulerJob(Long jobId) {
	    nodeSchedulerJobs.add(jobId);
    }

    public void removeSchedulerJob(Long jobId) {
	    nodeSchedulerJobs.remove(jobId);
    }

	/*
	 * 集群间通信的数据包队列
	 * 1. follower向leader发送 followerinfo
	 * 2. leader向follower发送leaderinfo
	 */
	private ConcurrentLinkedQueue<ClusterPacket<?>> clusterPackets = new ConcurrentLinkedQueue<ClusterPacket<?>>();
	
	public ClusterPacket<?> pollClusterPackets() {
		synchronized (clusterPackets) {
			return clusterPackets.poll();
		}
	}
	
	public void offerClusterPackets(ClusterPacket<?> packet) {
		synchronized (clusterPackets) {
			clusterPackets.offer(packet);
		}
	}
	
	// 存放所有followers调度的job
	private ConcurrentHashMap<Integer, Set<Long>> followerSchedulerJobs = new ConcurrentHashMap<>();
	
	public void upgradeFollowerSchedulerJobs(Integer nid, Set<Long> jobs) {
		synchronized (followerSchedulerJobs) {
			followerSchedulerJobs.put(nid, jobs);
		}
	}
	
	/*
	 * 从follower调度任务表中删除调度的任务
	 */
	public boolean removeFollowerSchedulerJob(Integer nid, Long jobId) {
		synchronized (followerSchedulerJobs) {
			Set<Long> jobs = followerSchedulerJobs.get(nid);
			if(null != jobs && jobs.size() > 0) {
				return jobs.remove(jobId);
			}
			log.warn("no job [] exist", jobId);
			return false;
		}
	}
	
	/*
	 * 向follower调度任务表中添加新的follower调度任务
	 */
	public void addFollowerSchedulerJob(Integer nid, Long jobId) {
		synchronized (followerSchedulerJobs) {
			if(followerSchedulerJobs.containsKey(nid)) {
				followerSchedulerJobs.get(nid).add(jobId);
			}
		}
	}
	
	/*
	 * 通过job id 找到对应的调度服务器的nid
	 */
	public Integer findFollowerNidByJob(Long jobId) {
		synchronized (followerSchedulerJobs) {
			for(Integer nid : followerSchedulerJobs.keySet()) {
				if(followerSchedulerJobs.get(nid).contains(jobId)) {
					return nid;
				}
			}
			return null;
		}
	}
	
}
