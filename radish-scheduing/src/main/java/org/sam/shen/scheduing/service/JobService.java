package org.sam.shen.scheduing.service;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

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
	
}
