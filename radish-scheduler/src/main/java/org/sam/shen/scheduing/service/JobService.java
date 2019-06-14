package org.sam.shen.scheduing.service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.scheduing.cluster.*;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.mapper.JobSchedulerMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Service
public class JobService {
	private Logger logger = LoggerFactory.getLogger(JobService.class);

	@Resource
	private JobInfoMapper jobInfoMapper;

	@Resource
    private JobSchedulerMapper jobSchedulerMapper;

	@Autowired
	private ClusterPeer clusterPeer;

	/**
	 * 添加任务
	 * @author suoyao
	 * @date 下午5:48:51
	 * @param jobInfo 任务信息
	 */
	@Transactional
	public void addJobinfo(JobInfo jobInfo) {
		// 插入数据库
		jobInfoMapper.saveJobInfo(jobInfo);
		// 加入任务调度
		if (jobInfo.getEnable() == Constant.YES && StringUtils.isNotEmpty(jobInfo.getExecutorHandlers()) && StringUtils.isNotEmpty(jobInfo.getCrontab())) {
		    try {
		        RadishDynamicScheduler.addJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
            } catch (SchedulerException e) {
		        logger.info("add job to Scheduler failed. {}", e.getMessage());
            }
		}
	}
	
	/**
	 *  修改JobInfo
	 * @author suoyao
	 * @date 下午5:15:45
	 * @param jobInfo
	 */
	@Transactional
	public void upgradeJobInfo(JobInfo jobInfo) {
		// 更新JobInfo信息
		jobInfoMapper.upgradeJonInfo(jobInfo);
		// 修改scheduler 调度
		if (jobInfo.getEnable() == Constant.YES && StringUtils.isNotEmpty(jobInfo.getExecutorHandlers())) {
            try {
                // 如果单机模式
                if (clusterPeer.getMyId() == null) {
                    if (StringUtils.isNotEmpty(jobInfo.getCrontab())) {
                        RadishDynamicScheduler.UpgradeScheduleJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
                    }
                    return;
                }
                // 如果该任务在当前节点运行则执行更新
                if (ClusterPeerNodes.getSingleton().getSchedulerJobsView().contains(jobInfo.getId())) {
                    if (StringUtils.isNotEmpty(jobInfo.getCrontab())) {
                        RadishDynamicScheduler.UpgradeScheduleJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
                    }
                } else if (jobSchedulerMapper.exist(jobInfo.getId()) == 0) { // 判断该任务是否还在运行
                    if (StringUtils.isNotEmpty(jobInfo.getCrontab())) {
                        RadishDynamicScheduler.addJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
                    }
                } else {
                    if (StringUtils.isNotEmpty(jobInfo.getCrontab())) {
                        LeaderInfo leaderInfo = new LeaderInfo(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
                        // 如果当前是leader节点，则向LeaderNode中queue中添加数据包，从节点则将LeaderInfo发送给leader节点作处理
                        if (clusterPeer.getNodeState() == ClusterPeer.NodeState.LEADING) {
                            ClusterPacket<LeaderInfo> packet = new ClusterPacket<>();
                            packet.setT(leaderInfo);
                            clusterPeer.leaderNode.queueFollowerPacket(packet);
                        } else if (clusterPeer.getNodeState() == ClusterPeer.NodeState.FOLLOWING) {
                            clusterPeer.followerNode.sendFollowerInfo(leaderInfo);
                        }
                    }
                }
            } catch (SchedulerException | IOException e) {
                logger.error("add job to Scheduler failed. {}", e.getMessage());
            }
		}
	}
	
	/**
	 *  分页查询JobInfo
	 * @author suoyao
	 * @date 下午6:16:54
	 * @param index
	 * @param limit
	 * @param jobName
	 * @return
	 */
	public Page<JobInfo> queryJobInfoForPager(int index, int limit, String jobName, Long userId) {
		PageHelper.startPage(index, limit);
		return jobInfoMapper.queryJobInfoForPager(jobName, userId);
	}
	
	public List<JobInfo> queryJobInfoForList(String jobName, Long userId) {
		return jobInfoMapper.queryJobInfoForList(jobName, userId);
	}
	
	public JobInfo findJobInfo(Long id) {
		return jobInfoMapper.findJobInfoById(id);
	}
	
	public Map<String, Set<String>> dagre(JobInfo jobInfo) {
		Set<String> nodes = Sets.newHashSet(jobInfo.getJobName());
		Set<String> edges = Sets.newHashSet();
		forDagre(nodes, edges, jobInfo);
		return new HashMap<String, Set<String>>() {
			private static final long serialVersionUID = 3018401508302256817L;
			{
				put("nodes", nodes);
				put("edges", edges);
			}
		};
	}
	
	public List<JobInfo> queryJobInfoByIds(List<Long> ids) {
		if(null == ids || ids.isEmpty()) {
			return Collections.emptyList();
		}
		return jobInfoMapper.queryJobInfoInIds(ids, null);
	}
	
	/**
	 * 递归实现节点和边缘关系
	 * @author suoyao
	 * @date 上午10:58:46
	 * @param nodes
	 * @param edges
	 * @param jobInfo
	 */
	private void forDagre(Set<String> nodes, Set<String> edges, JobInfo jobInfo) {
		List<Long> ids = Lists.newArrayList();
		if(StringUtils.isNotEmpty(jobInfo.getParentJobId())) {
			Splitter.on(",").splitToList(jobInfo.getParentJobId()).forEach(id -> ids.add(Long.valueOf(id)));
		}
		if(ids.size() > 0) {
			List<JobInfo> depend = jobInfoMapper.queryJobInfoInIds(ids, null);
			if(null != depend && depend.size() > 0) {
				depend.forEach(jf -> {
					nodes.add(jf.getJobName());
					String arrow = null;
					if (null != jobInfo.getParentJobId() && jobInfo.getParentJobId().contains(String.valueOf(jf.getId()))) {
						arrow = jf.getJobName() + Constant.SPLIT_CHARACTER_ARROW.concat(jobInfo.getJobName());
					}
					if(StringUtils.isNotEmpty(arrow) && !edges.contains(arrow)) {
						edges.add(arrow);
						forDagre(nodes, edges, jf);
					}
				});
			}
		}
	}

	/**
	 * Get child job information by parent job id
	 * @author clock
	 * @date 2018/12/4 下午3:25
	 * @param jobId job id
	 * @return job information
	 */
	public List<JobInfo> getChildJobByParentJobId(String jobId) {
		return jobInfoMapper.findJobInfoByParentId(jobId);
	}

	public boolean removeJob(Long jobId) {
	    int count = jobInfoMapper.deleteJobById(jobId);
	    return count > 0;
    }
	
}
