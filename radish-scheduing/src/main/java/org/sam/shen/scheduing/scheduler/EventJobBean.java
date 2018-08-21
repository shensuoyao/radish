package org.sam.shen.scheduing.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sam.shen.scheduing.entity.JobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  执行Job Callback的bean
 * @author suoyao
 * @date 2018年8月16日 下午2:45:49
  * 
 */
public class EventJobBean implements Job {
	private static final Logger logger = LoggerFactory.getLogger(EventJobBean.class);

	private Long jobId;
	
	public EventJobBean() {
		super();
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		init(context);
		if(logger.isInfoEnabled()) {
			logger.info("Hello World!  MyJob is executing.");
		}
		// JobInfo jobInfo = (JobInfo) dataMap.get("jobInfo");
		logger.info("job id is : {}", jobId);
		// 1. load JobInfo
		JobInfo jobInfo = RadishDynamicScheduler.jobInfoMapper.findJobInfoById(jobId);
		// 2. 检查JobInfo的enable状态是否为启用
		// 如果为禁用状态, 则从调度器中删除该任务的调度
		// TODO
		logger.info(jobInfo.toString());
		logger.info("============");
		destory();
	}  
	
	public void init(JobExecutionContext context) {
		if(logger.isInfoEnabled()) {
			logger.info("init job...");
		}
		context.getMergedJobDataMap();
	}

	public void destory() {
		if(logger.isInfoEnabled()) {
			logger.info("destory job...");
		}
		// TODO
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
	
}
