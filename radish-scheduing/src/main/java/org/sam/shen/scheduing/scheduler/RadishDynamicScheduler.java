package org.sam.shen.scheduing.scheduler;

import java.util.Date;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.sam.shen.scheduing.entity.JobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public final class RadishDynamicScheduler implements ApplicationContextAware {
	private static final Logger logger = LoggerFactory.getLogger(RadishDynamicScheduler.class);
	
	// scheduler
	@Autowired
	private static Scheduler scheduler;
	
	public void setScheduler(Scheduler scheduler) {
		RadishDynamicScheduler.scheduler = scheduler;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		RadishDynamicScheduler.scheduler = applicationContext.getBean(Scheduler.class);
	}
	
	@PostConstruct
	public void init() throws Exception {
		scheduler.start();
	}
	
	@PreDestroy
	public void destroy() {
		// TODO
	}
	
	public static boolean addJob(JobInfo jobInfo) throws SchedulerException {
		// TriggerKey valid if_exists
		String qz_name = String.valueOf(jobInfo.getId());
		String qz_group = String.valueOf(jobInfo.getJobName().hashCode());
		if (checkExists(qz_name, qz_group)) {
			logger.info(">>>>>>>>> addJob fail, job already exist, jobGroup:{}, jobName:{}", qz_group, qz_name);
			return false;
		}
		
		// CronTrigger : TriggerKey + cronExpression //
		// withMisfireHandlingInstructionDoNothing 忽略掉调度终止过程中忽略的调度
		TriggerKey triggerKey = TriggerKey.triggerKey(qz_name, qz_group);
		CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(jobInfo.getCron())
		        .withMisfireHandlingInstructionDoNothing();
		CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
		        .build();

		// Trigger the job to run with cron
		JobKey jobKey = new JobKey(qz_name, qz_group);
		Class<? extends Job> jobClass_ = CallbackJobBean.class;
		@SuppressWarnings("serial")
		JobDataMap jobDataMap = new JobDataMap(new HashMap<String, JobInfo>(){
			{
				put("jobInfo", jobInfo);
			}
		}) ;
		JobDetail jobDetail = JobBuilder.newJob(jobClass_).withIdentity(jobKey).usingJobData(jobDataMap).build();
		
		// Tell quartz to schedule the job using our trigger
		Date date = scheduler.scheduleJob(jobDetail, cronTrigger);
		if(logger.isInfoEnabled()) {
			logger.info(">>>>>>>>>>> addJob success, jobDetail:{}, cronTrigger:{}, date:{}", jobDetail, cronTrigger, date);
		}
		return true;
	}
	
	
	public static boolean checkExists(String jobName, String jobGroup) throws SchedulerException{
		TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
		return scheduler.checkExists(triggerKey);
	}
}
