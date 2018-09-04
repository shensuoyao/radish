package org.sam.shen.scheduing.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	/**
	 * 添加任务
	 * @author suoyao
	 * @date 下午5:48:51
	 * @param jobInfo
	 */
	@Transactional
	public void addJobinfo(JobInfo jobInfo) {
		// 插入数据库
		jobInfoMapper.saveJobInfo(jobInfo);
		// 加入任务调度
		if (jobInfo.getEnable() == Constant.YES && StringUtils.isNotEmpty(jobInfo.getCrontab())
		        && StringUtils.isNotEmpty(jobInfo.getExecutorHandlers())) {
			try {
				RadishDynamicScheduler.addJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
			} catch (SchedulerException e) {
				logger.error("add job to Scheduler failed. {}", e);
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
		if (jobInfo.getEnable() == Constant.YES && StringUtils.isNotEmpty(jobInfo.getCrontab())
		        && StringUtils.isNotEmpty(jobInfo.getExecutorHandlers())) {
			try {
				RadishDynamicScheduler.UpgradeScheduleJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
			} catch (SchedulerException e) {
				logger.error("add job to Scheduler failed. {}", e);
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
	public Page<JobInfo> queryJobInfoForPager(int index, int limit, String jobName) {
		PageHelper.startPage(index, limit);
		return jobInfoMapper.queryJobInfoForPager(jobName);
	}
	
	public List<JobInfo> queryJobInfoForList(String jobName) {
		return jobInfoMapper.queryJobInfoForList(jobName);
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
		return jobInfoMapper.queryJobInfoInIds(ids);
	}
	
	/**
	 * 递归实现节点和边缘关系
	 * @author suoyao
	 * @date 上午10:58:46
	 * @param nodes
	 * @param edges
	 * @param jobInfo
	 */
	public void forDagre(Set<String> nodes, Set<String> edges, JobInfo jobInfo) {
		List<Long> ids = Lists.newArrayList();
		if(StringUtils.isNotEmpty(jobInfo.getParentJobId())) {
			Splitter.on(",").splitToList(jobInfo.getParentJobId()).forEach(id -> ids.add(Long.valueOf(id)));
		}
		if(StringUtils.isNotEmpty(jobInfo.getChildJobId())) {
			Splitter.on(",").splitToList(jobInfo.getChildJobId()).forEach(id -> ids.add(Long.valueOf(id)));
		}
		if(ids.size() > 0) {
			List<JobInfo> depend = jobInfoMapper.queryJobInfoInIds(ids);
			if(null != depend && depend.size() > 0) {
				depend.forEach(jf -> {
					nodes.add(jf.getJobName());
					String arrow = null;
					if (null != jobInfo.getParentJobId() && jobInfo.getParentJobId().indexOf(String.valueOf(jf.getId())) >= 0) {
						arrow = jf.getJobName() + Constant.SPLIT_CHARACTER_ARROW.concat(jobInfo.getJobName());
					}
					if(null != jobInfo.getChildJobId() && jobInfo.getChildJobId().indexOf(String.valueOf(jf.getId())) >= 0) {
						arrow = jobInfo.getJobName() + Constant.SPLIT_CHARACTER_ARROW.concat(jf.getJobName());
					}
					if(StringUtils.isNotEmpty(arrow) && !edges.contains(arrow)) {
						edges.add(arrow);
						forDagre(nodes, edges, jf);
					}
				});
			}
		}
	}
	
}
