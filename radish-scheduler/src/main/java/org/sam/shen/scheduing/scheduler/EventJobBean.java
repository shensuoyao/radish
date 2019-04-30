package org.sam.shen.scheduing.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.sam.shen.scheduing.entity.JobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StringUtils;

/**
 *  执行Job Callback的bean
 * @author suoyao
 * @date 2018年8月16日 下午2:45:49
  * 
 */
public class EventJobBean extends QuartzJobBean {
	private static final Logger logger = LoggerFactory.getLogger(EventJobBean.class);

	private Long jobId;

	private String jobName;
	
	public EventJobBean() {
		super();
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		init(context);
		// JobInfo jobInfo = (JobInfo) dataMap.get("jobInfo");
		// 1. load JobInfo
		JobInfo jobInfo = RadishDynamicScheduler.jobInfoMapper.findJobInfoById(jobId);
		// 2. 检查JobInfo的enable状态是否为启用，或者Job是否变为手动执行
		if(jobInfo == null || jobInfo.getEnable() != 1 || StringUtils.isEmpty(jobInfo.getCrontab())) {
			if(logger.isInfoEnabled()) {
				logger.info("job is disabled {}", jobName);
			}
			// 禁用状态, 则从调度器中删除该任务的调度
			try {
				RadishDynamicScheduler.removeJob(jobId, jobName);
				// TODO: 2018/11/21 关闭执行中的线程，针对没有调度周期无限循环的调度任务
			} catch (SchedulerException e) {
				logger.error("remove job [" + jobName + "] from scheduler failed", e);
			}
		} else {
			// 3. 发送Event事件到抢占任务事件队列
			RadishDynamicScheduler.addJobEventWithChildren(jobInfo);
		}
		destroy();
	}
	
	public void init(JobExecutionContext context) {
		context.getMergedJobDataMap();
		if (logger.isInfoEnabled()) {
			logger.info("init job ==== {} =====", jobId);
		}
	}

	public void destroy() {
		if(logger.isInfoEnabled()) {
			logger.info("destroy job ==== {} =====", jobId);
		}
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

}
