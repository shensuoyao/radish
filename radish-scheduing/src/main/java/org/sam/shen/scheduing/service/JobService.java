package org.sam.shen.scheduing.service;

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
		if(StringUtils.isNotEmpty(jobInfo.getCrontab())) {
			try {
				RadishDynamicScheduler.addJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
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
		Set<String> node = Sets.newHashSet(jobInfo.getJobName());
		Set<String> edge = Sets.newHashSet();
		forDagre(node, edge, jobInfo);
		return new HashMap<String, Set<String>>() {
			private static final long serialVersionUID = 3018401508302256817L;
			{
				put("nodes", node);
				put("edges", edge);
			}
		};
	}
	
	public void forDagre(Set<String> jobSet, Set<String> edge, JobInfo jobInfo) {
		jobSet.add(jobInfo.getJobName());
		if(StringUtils.isNotEmpty(jobInfo.getParentJobId())) {
			List<Long> ids = Lists.newArrayList();
			Splitter.on(",").splitToList(jobInfo.getParentJobId()).forEach(id -> ids.add(Long.valueOf(id)));
			List<JobInfo> depend = jobInfoMapper.queryJobInfoByIds(ids);
			if(null != depend && depend.size() > 0) {
				depend.forEach(jf -> {
					jobSet.add(jf.getJobName());
					String arrow = jf.getJobName() + Constant.SPLIT_CHARACTER_ARROW.concat(jobInfo.getJobName());
					if(!edge.contains(arrow)) {
						edge.add(arrow);
						forDagre(jobSet, edge, jf);
					}
				});
			}
		}
		if(StringUtils.isNotEmpty(jobInfo.getChildJobId())) {
			List<Long> ids = Lists.newArrayList();
			Splitter.on(",").splitToList(jobInfo.getChildJobId()).forEach(id -> ids.add(Long.valueOf(id)));
			List<JobInfo> depend = jobInfoMapper.queryJobInfoByIds(ids);
			if(null != depend && depend.size() > 0) {
				depend.forEach(jf -> {
					jobSet.add(jf.getJobName());
					String arrow = jobInfo.getJobName() + Constant.SPLIT_CHARACTER_ARROW.concat(jf.getJobName());
					if(!edge.contains(arrow)) {
						edge.add(arrow);
						forDagre(jobSet, edge, jf);
					}
				});
			}
		}
	}
	
}
