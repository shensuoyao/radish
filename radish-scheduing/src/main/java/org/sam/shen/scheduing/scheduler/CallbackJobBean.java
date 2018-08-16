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
public class CallbackJobBean implements Job {
	private static final Logger logger = LoggerFactory.getLogger(CallbackJobBean.class);

	private JobInfo jobInfo;
	
	public CallbackJobBean() {
		super();
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		init(context);
		if(logger.isInfoEnabled()) {
			logger.info("Hello World!  MyJob is executing.");
		}
		// JobInfo jobInfo = (JobInfo) dataMap.get("jobInfo");
		logger.info("job name is : {}", jobInfo.getJobName());
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

	public void setJobInfo(JobInfo jobInfo) {
		this.jobInfo = jobInfo;
	}
	
}
