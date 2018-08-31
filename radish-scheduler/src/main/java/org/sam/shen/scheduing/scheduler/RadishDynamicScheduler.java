package org.sam.shen.scheduing.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

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
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.sam.shen.scheduing.mapper.JobEventMapper;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public final class RadishDynamicScheduler implements ApplicationContextAware {
	private static final Logger logger = LoggerFactory.getLogger(RadishDynamicScheduler.class);
	
	// scheduler
	private static Scheduler scheduler;
	
	public static JobInfoMapper jobInfoMapper;
	
	public static JobEventMapper jobEventMapper;
	
	public static RedisService redisService;
	
	private RadishDynamicScheduler() {
		super();
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		RadishDynamicScheduler.scheduler = applicationContext.getBean("quartzScheduler", Scheduler.class);
		RadishDynamicScheduler.jobInfoMapper = applicationContext.getBean(JobInfoMapper.class);
		RadishDynamicScheduler.redisService = applicationContext.getBean(RedisService.class);
		RadishDynamicScheduler.jobEventMapper = applicationContext.getBean(JobEventMapper.class);
	}
	
	@PostConstruct
	public void init() throws Exception {
		// valid
		Assert.notNull(scheduler, "quartz scheduler is null");
		logger.info(">>>>>>>>> init job-scheduler success.");
	}
	
	@PreDestroy
	public void destroy() {
		// TODO
	}
	
	/**
	 *  添加Job任务
	 * @author suoyao
	 * @date 下午5:52:11
	 * @param jobId
	 * @param jobName
	 * @param crontab
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean addJob(final Long jobId, final String jobName, final String crontab) throws SchedulerException {
		// TriggerKey valid if_exists
		if (checkExists(jobId, jobName)) {
			logger.info(">>>>>>>>> addJob fail, job already exist, jobGroup:{}, jobName:{}", jobId, jobName.hashCode());
			return false;
		}
		
		// CronTrigger : TriggerKey + crontab //
		// withMisfireHandlingInstructionDoNothing 忽略掉调度终止过程中忽略的调度
		TriggerKey triggerKey = getTriggerKey(jobId, jobName);
		CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(crontab)
		        .withMisfireHandlingInstructionDoNothing();
		CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
		        .build();

		// Trigger the job to run with cron
		JobKey jobKey = new JobKey(String.valueOf(jobId), String.valueOf(jobName.hashCode()));
		Class<? extends Job> jobClass_ = EventJobBean.class;
		JobDataMap jobDataMap = new JobDataMap(new HashMap<String, Long>(){
			private static final long serialVersionUID = 1L;
			{
				put("jobId", jobId);
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
	
	/**
	 *  更新Schedule Job
	 * @author suoyao
	 * @date 下午5:11:52
	 * @param jobId
	 * @param jobName
	 * @param crontab
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean UpgradeScheduleJob(final Long jobId, final String jobName, final String crontab) throws SchedulerException {
		// TriggerKey valid if_exists
		if (!checkExists(jobId, jobName)) {
			logger.error(">>>>>>>>>>> Upgrade ScheduleJob, job not exists, JobGroup:{}, JobName:{}", jobId, jobName.hashCode());
			addJob(jobId, jobName, crontab);
			return true;
		}
		// TriggerKey : name + group
		TriggerKey triggerKey = getTriggerKey(jobId, jobName);
		CronTrigger oldTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
		if(null != oldTrigger) {
			// 存在久的触发器
			// avoid repeat
			String oldCron = oldTrigger.getCronExpression();
			if (oldCron.equals(crontab)) {
				return true;
			}
			// CronTrigger : TriggerKey + crontab
			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(crontab)
			        .withMisfireHandlingInstructionDoNothing();
			oldTrigger = oldTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
			        .build();

			// rescheduleJob
			scheduler.rescheduleJob(triggerKey, oldTrigger);
		} else {
			// CronTrigger : TriggerKey + crontab
			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(crontab)
			        .withMisfireHandlingInstructionDoNothing();
			CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
			        .withSchedule(cronScheduleBuilder).build();

			// JobDetail-JobDataMap fresh
			JobKey jobKey = new JobKey(String.valueOf(jobId), String.valueOf(jobName.hashCode()));
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);

			// Trigger fresh
			HashSet<Trigger> triggerSet = new HashSet<Trigger>();
			triggerSet.add(cronTrigger);

			scheduler.scheduleJob(jobDetail, triggerSet, true);
		}
		if(logger.isInfoEnabled()) {
			logger.info(">>>>>>>>>>> resumeJob success, JobGroup:{}, JobName:{}", jobId, jobName.hashCode());
		}
		return true;
	}
	
	/**
	 * 移除 schedule Job
	 * @author suoyao
	 * @date 下午5:13:37
	 * @param jobId
	 * @param jobName
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean removeJob(final Long jobId, final String jobName) throws SchedulerException {
		// TriggerKey : name + group
		TriggerKey triggerKey = getTriggerKey(jobId, jobName);
		boolean result = false;
		if (checkExists(jobId, jobName)) {
			result = scheduler.unscheduleJob(triggerKey);
			logger.info(">>>>>>>>>>> removeJob, triggerKey:{}, result [{}]", triggerKey, result);
		}
		return true;
	}
	
	/**
	 *  暂停Job任务
	 * @author suoyao
	 * @date 下午5:27:25
	 * @param jobId
	 * @param jobName
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean pauseJob(final Long jobId, final String jobName) throws SchedulerException {
		// TriggerKey : name + group
		TriggerKey triggerKey = getTriggerKey(jobId, jobName);
		boolean result = false;
		if (checkExists(jobId, jobName)) {
			scheduler.pauseTrigger(triggerKey);
			result = true;
			logger.info(">>>>>>>>>>> pauseJob success, triggerKey:{}", triggerKey);
		} else {
			logger.info(">>>>>>>>>>> pauseJob fail, triggerKey:{}", triggerKey);
		}
		return result;
	}
    
	/**
	 *  重启Job任务
	 * @author suoyao
	 * @date 下午5:27:07
	 * @param jobId
	 * @param jobName
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean resumeJob(final Long jobId, final String jobName) throws SchedulerException {
		// TriggerKey : name + group
		TriggerKey triggerKey = getTriggerKey(jobId, jobName);

		boolean result = false;
		if (checkExists(jobId, jobName)) {
			scheduler.resumeTrigger(triggerKey);
			result = true;
			logger.info(">>>>>>>>>>> resumeJob success, triggerKey:{}", triggerKey);
		} else {
			logger.info(">>>>>>>>>>> resumeJob fail, triggerKey:{}", triggerKey);
		}
		return result;
	}
	
	public static boolean checkExists(final Long jobId, final String jobName) throws SchedulerException{
		TriggerKey triggerKey = getTriggerKey(jobId, jobName);
		return scheduler.checkExists(triggerKey);
	}
	
	private static TriggerKey getTriggerKey(final Long jobId, final String jobName) {
		return TriggerKey.triggerKey(String.valueOf(jobId), String.valueOf(jobName.hashCode()));
	}
	
}