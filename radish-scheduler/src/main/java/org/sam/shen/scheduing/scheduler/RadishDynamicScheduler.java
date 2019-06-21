package org.sam.shen.scheduing.scheduler;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
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
import org.quartz.impl.matchers.GroupMatcher;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.scheduing.cluster.ClusterPeerConfig;
import org.sam.shen.scheduing.cluster.ClusterPeerNodes;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.entity.JobScheduler;
import org.sam.shen.scheduing.mapper.JobEventMapper;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.mapper.JobSchedulerMapper;
import org.sam.shen.scheduing.service.RedisService;
import org.sam.shen.scheduing.strategy.DistributionStrategyFactory;
import org.sam.shen.scheduing.vo.JobSchedulerVo;
import org.sam.shen.scheduing.vo.SchedulerJobVo;
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
	
	private static JobInfoMapper jobInfoMapper;
	
	public static JobInfoMapper getJobInfoMapper() {
		return jobInfoMapper;
	}
	
	private static JobEventMapper jobEventMapper;
	
	private static RedisService redisService;

	private static JobSchedulerMapper jobSchedulerMapper;

    private static ApplicationContext applicationContext;

	private RadishDynamicScheduler() {
		super();
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	    RadishDynamicScheduler.applicationContext = applicationContext;
		RadishDynamicScheduler.scheduler = applicationContext.getBean("quartzScheduler", Scheduler.class);
		RadishDynamicScheduler.jobInfoMapper = applicationContext.getBean(JobInfoMapper.class);
		RadishDynamicScheduler.redisService = applicationContext.getBean(RedisService.class);
		RadishDynamicScheduler.jobEventMapper = applicationContext.getBean(JobEventMapper.class);
		RadishDynamicScheduler.jobSchedulerMapper = applicationContext.getBean(JobSchedulerMapper.class);
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
     * update job information by cron trigger
     * @author clock
     * @date 2019/3/13 下午3:15
     * @param jobId job ID
     * @param status running status
     * @param prevTime previous fire time
     * @param nextTime next fire time
     */
	private static void updateRunningStatus(Long jobId, JobScheduler.RunningStatus status, Date prevTime, Date nextTime) {
        JobScheduler jobScheduler = new JobScheduler(jobId, status, prevTime, nextTime);
        jobSchedulerMapper.changeRunningStatus(jobScheduler);
    }
	
	/**
	 *  添加Job任务
	 * @author suoyao
	 * @date 下午5:52:11
	 * @param jobId
	 * @param createTime
	 * @param crontab
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean addJob(final Long jobId, final Long createTime, final String crontab) throws SchedulerException {
		// TriggerKey valid if_exists
		if (checkExists(jobId, createTime)) {
			logger.info(">>>>>>>>> addJob fail, job already exist, jobId:{}, createTime:{}", jobId, createTime);
			return false;
		}
		
		// CronTrigger : TriggerKey + crontab //
		// withMisfireHandlingInstructionDoNothing 忽略掉调度终止过程中忽略的调度
		TriggerKey triggerKey = getTriggerKey(jobId, createTime);
		CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(crontab)
		        .withMisfireHandlingInstructionDoNothing();
		CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
		        .build();

		// Trigger the job to run with cron
		JobKey jobKey = new JobKey(String.valueOf(jobId), String.valueOf(createTime));
		Class<? extends Job> jobClass_ = EventJobBean.class;
		JobDataMap jobDataMap = new JobDataMap(new HashMap<String, Object>(){
			private static final long serialVersionUID = 1L;
			{
				put("jobId", jobId);
				put("createTime", createTime);
			}
		});
		JobDetail jobDetail = JobBuilder.newJob(jobClass_).withIdentity(jobKey).usingJobData(jobDataMap).build();
		
		// Tell quartz to schedule the job using our trigger
		Date date = scheduler.scheduleJob(jobDetail, cronTrigger);

		// save job scheduler
        Integer nid = applicationContext.getBean(ClusterPeerConfig.class).getNid();
        jobSchedulerMapper.delete(jobId);
        jobSchedulerMapper.insert(new JobScheduler(jobId, nid, JobScheduler.RunningStatus.RUNNING, cronTrigger.getPreviousFireTime(), cronTrigger.getNextFireTime()));
        // add scheduler to cluster peer nodes
        ClusterPeerNodes.getSingleton().addSchedulerJob(jobId);

		if(logger.isInfoEnabled()) {
			logger.info(">>>>>>>>>>> addJob success, jobDetail:{}, cronTrigger:{}, date:{}", jobDetail, cronTrigger, date);
		}
		return true;
	}
	
	/**
	 * 更新Schedule Job
	 * @author suoyao
	 * @date 下午5:11:52
	 * @param jobId job ID
	 * @param createTime create time
	 * @param crontab crontab expression
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean UpgradeScheduleJob(final Long jobId, final Long createTime, final String crontab) throws SchedulerException {
		// TriggerKey valid if_exists
		if (!checkExists(jobId, createTime)) {
			logger.error(">>>>>>>>>>> Upgrade ScheduleJob, job not exists, jobId:{}, createTime:{}", jobId, createTime);
			addJob(jobId, createTime, crontab);
			return true;
		}
		// TriggerKey : name + group
		TriggerKey triggerKey = getTriggerKey(jobId, createTime);
		CronTrigger oldTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
		if(null != oldTrigger) {
			// 存在旧的触发器
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
            // update job information
            updateRunningStatus(jobId, JobScheduler.RunningStatus.RUNNING, oldTrigger.getPreviousFireTime(), oldTrigger.getNextFireTime());
		} else {
			// CronTrigger : TriggerKey + crontab
			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(crontab)
			        .withMisfireHandlingInstructionDoNothing();
			CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
			        .withSchedule(cronScheduleBuilder).build();

			// JobDetail-JobDataMap fresh
			JobKey jobKey = new JobKey(String.valueOf(jobId), String.valueOf(createTime));
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);

			// Trigger fresh
			HashSet<Trigger> triggerSet = new HashSet<Trigger>();
			triggerSet.add(cronTrigger);

			scheduler.scheduleJob(jobDetail, triggerSet, true);
            // update job information
            updateRunningStatus(jobId, JobScheduler.RunningStatus.RUNNING, cronTrigger.getPreviousFireTime(), cronTrigger.getNextFireTime());
		}
		if(logger.isInfoEnabled()) {
			logger.info(">>>>>>>>>>> resumeJob success, JobId:{}, CreateTime:{}", jobId, createTime);
		}
		return true;
	}
	
	/**
	 * 移除 schedule Job
	 * @author suoyao
	 * @date 下午5:13:37
	 * @param jobId
	 * @param createTime
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean removeJob(final Long jobId, final Long createTime) throws SchedulerException {
		// TriggerKey : name + group
		TriggerKey triggerKey = getTriggerKey(jobId, createTime);
		boolean result;
		if (checkExists(jobId, createTime)) {
			result = scheduler.unscheduleJob(triggerKey);
            // delete job scheduler
            jobSchedulerMapper.delete(jobId);
            // remove job from cluster peer nodes
            ClusterPeerNodes.getSingleton().removeSchedulerJob(jobId);
			logger.info(">>>>>>>>>>> removeJob, triggerKey:{}, result [{}]", triggerKey, result);
		}
		return true;
	}
	
	/**
	 *  暂停Job任务
	 * @author suoyao
	 * @date 下午5:27:25
	 * @param jobId
	 * @param createTime
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean pauseJob(final Long jobId, final Long createTime) throws SchedulerException {
		// TriggerKey : name + group
		TriggerKey triggerKey = getTriggerKey(jobId, createTime);
		boolean result = false;
		if (checkExists(jobId, createTime)) {
			scheduler.pauseTrigger(triggerKey);
            // update job scheduler
            updateRunningStatus(jobId, JobScheduler.RunningStatus.PAUSED, null, null);
            // remove job from cluster peer nodes
            ClusterPeerNodes.getSingleton().removeSchedulerJob(jobId);
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
	 * @param createTime
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean resumeJob(final Long jobId, final Long createTime) throws SchedulerException {
		// TriggerKey : name + group
		TriggerKey triggerKey = getTriggerKey(jobId, createTime);

		boolean result = false;
		if (checkExists(jobId, createTime)) {
			scheduler.resumeTrigger(triggerKey);
            // update job information
			CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            updateRunningStatus(jobId, JobScheduler.RunningStatus.RUNNING, null, cronTrigger.getNextFireTime());
            // add scheduler to cluster peer nodes
            ClusterPeerNodes.getSingleton().addSchedulerJob(jobId);
			result = true;
			logger.info(">>>>>>>>>>> resumeJob success, triggerKey:{}", triggerKey);
		} else {
			logger.info(">>>>>>>>>>> resumeJob fail, triggerKey:{}", triggerKey);
		}
		return result;
	}
	
	public static boolean checkExists(final Long jobId, final Long createTime) throws SchedulerException{
		TriggerKey triggerKey = getTriggerKey(jobId, createTime);
		return scheduler.checkExists(triggerKey);
	}
	
	private static TriggerKey getTriggerKey(final Long jobId, final Long createTime) {
		return TriggerKey.triggerKey(String.valueOf(jobId), String.valueOf(createTime));
	}
	
	public static List<JobSchedulerVo> listJobsInScheduler(Long userId) {
	    List<JobSchedulerVo> jobs = jobSchedulerMapper.queryJobScheduler(null, JobScheduler.RunningStatus.RUNNING, userId, null, null);
	    return jobs == null ? Collections.emptyList() : jobs;
	}

	public static Page<JobSchedulerVo> listJobsInSchedulerWithPage(String jobName, Long userId, int pageIndex, int pageSize, String field, String order) {
		PageHelper.startPage(pageIndex, pageSize);
		return jobSchedulerMapper.queryJobScheduler(jobName, JobScheduler.RunningStatus.RUNNING, userId, field, order);
	}

	@SuppressWarnings("unchecked")
	public static List<SchedulerJobVo> listJobsInScheduler() throws SchedulerException {
		Map<String, SchedulerJobVo> jobMap = new HashMap<>();
		for(String groupName: scheduler.getJobGroupNames()) {
			// enumerate each job in group
			for(JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
				System.out.println("Found job identified by: " + jobKey);
				System.out.println("jobName is : " + jobKey.getName());
				System.out.println("jobGroup is : " + jobKey.getName());
				SchedulerJobVo vo = new SchedulerJobVo(jobKey.getName(), jobKey.getGroup());
				List<CronTrigger> triggers = (List<CronTrigger>) scheduler.getTriggersOfJob(jobKey);
				if(null != triggers && triggers.size() > 0) {
					vo.setCrontab(triggers.get(0).getCronExpression());
					vo.setPrevFireTime(triggers.get(0).getPreviousFireTime());
					vo.setNextFireTime(triggers.get(0).getNextFireTime());
				}
				jobMap.put(jobKey.getName(), vo);
			}
		}
		return new ArrayList<>(jobMap.values());
	}

    /**
     * Basic method that add job event
     * @author clock
     * @date 2018/12/12 上午10:17
     * @param jobInfo job information
     * @param parentEventId parent event id
     * @return execute result
     */
    private static List<JobEvent> addJobEventBase(JobInfo jobInfo, String parentEventId, String parentGroupId) {
        List<JobEvent> jobEvents = new ArrayList<>();
        // 如果存在分片规则，需要根据规则将job拆分为多个event
        if (jobInfo.getDistType() != null && StringUtils.isNotEmpty(jobInfo.getDistRule())) {
            jobEvents = DistributionStrategyFactory.newInstance(jobInfo.getDistType())
                    .distribute(jobInfo, EventStatus.READY, parentEventId, parentGroupId);
        } else {
            JobEvent jobEvent = new JobEvent(jobInfo.getId(), jobInfo.getExecutorHandlers(), jobInfo.getHandlerType(),
                    EventStatus.READY, jobInfo.getPriority(), jobInfo.getCmd(), jobInfo.getParams());
            jobEvent.setParentJobId(jobInfo.getParentJobId());
            jobEvent.setParentEventId(parentEventId);
            jobEvent.setParentGroupId(parentGroupId);
            jobEvent.setParamFilePath(jobInfo.getParamFilePath());
            jobEvents.add(jobEvent);
        }
        if (jobEvents.size() > 0) {
            jobEventMapper.batchInsert(jobEvents);
            List<String> agentHandlers = Splitter.onPattern(",|-").splitToList(jobInfo.getExecutorHandlers());
            Map<String, Object> eventHash = Maps.newHashMap();
            eventHash.put("priority", jobInfo.getPriority());    // 设置优先级
            Stream.iterate(0, i -> i + 1).limit(agentHandlers.size()).forEach(i -> {
                if (i % 2 == 0) {
                    if (eventHash.containsKey(agentHandlers.get(i))) {
                        String val = String.valueOf(eventHash.get(agentHandlers.get(i))).concat(",").concat(agentHandlers.get(i + 1));
                        eventHash.put(agentHandlers.get(i), val);
                    } else {
                        eventHash.put(agentHandlers.get(i), agentHandlers.get(i + 1));
                    }
                }
            });
            // 循环保存到redis缓存中
            for (JobEvent jobEvent : jobEvents) {
                redisService.hmset(Constant.REDIS_EVENT_PREFIX.concat(jobEvent.getEventId()), eventHash);
            }
        }
        return jobEvents;
    }

    /**
     * Add job event
     * @author clock
     * @date 2018/12/4 下午3:23
     * @param jobInfo job information
     * @return add result
     */
	public static boolean addJobEvent(JobInfo jobInfo) {
	    try {
            addJobEventBase(jobInfo, null, null);
        } catch (Exception e) {
	        return false;
        }
        return true;
    }

	/**
	 * Add job event with parent event id
	 * @author clock
	 * @date 2018/12/4 下午3:23
	 * @param jobInfo job information
	 * @param parentEventId parent event id
	 * @return add result
	 */
	public static boolean addJobEvent(JobInfo jobInfo, String parentEventId, String parentGroupId) {
        try {
            addJobEventBase(jobInfo, parentEventId, parentGroupId);
        } catch (Exception e) {
            return false;
        }
        return true;
	}

	public static boolean addJobEvents(List<JobInfo> jobInfos, String parentEventId, String parentGroupId) {
	    try {
            for (JobInfo jobInfo : jobInfos) {
                addJobEventBase(jobInfo, parentEventId, parentGroupId);
            }
        } catch (Exception e) {
	        return false;
        }
        return true;
    }

    /**
     * Add job event to execute queue, and add all subevents
     * @author clock
     * @date 2018/12/19 下午5:24
     * @param jobInfo job information
     */
    public static void addJobEventWithChildren(JobInfo jobInfo) {
        List<JobEvent> root = addJobEventBase(jobInfo, null, null);
        List<JobEvent> jobEvents = new ArrayList<>();
        List<JobEvent> parentEvents = new ArrayList<>();
        // 如果job存在分片规则，只需要添加一个job event就可以
        if (root.size() > 0) {
            parentEvents.add(root.get(0));
        }

        while (parentEvents.size() > 0) {
            List<JobEvent> tempEvents = new ArrayList<>();
            for (JobEvent parent : parentEvents) {
                List<JobInfo> children = jobInfoMapper.findJobInfoByParentId(String.valueOf(parent.getJobId()));
                if (children != null && children.size() > 0) {
                    for (JobInfo job : children) {
                        List<JobEvent> events = new ArrayList<>();
                        String parentEventId = null,
                                parentGroupId = null;
                        // 如果父事件是以group的形式，则需要设置parent group id
                        if (StringUtils.isNotEmpty(parent.getGroupId())) {
                            parentGroupId = parent.getGroupId();
                        } else {
                            parentEventId = parent.getEventId();
                        }
                        if (job.getDistType() != null && StringUtils.isNotEmpty(job.getDistRule())) {
                            events = DistributionStrategyFactory.newInstance(job.getDistType())
                                    .distribute(job, EventStatus.WAIT, parentEventId, parentGroupId);
                        } else {
                            JobEvent jobEvent = new JobEvent(job.getId(), job.getExecutorHandlers(), job.getHandlerType(),
                                    EventStatus.WAIT, job.getPriority(), job.getCmd(), job.getParams());
                            jobEvent.setParentEventId(parentEventId);
                            jobEvent.setParentJobId(job.getParentJobId());
                            jobEvent.setParentGroupId(parentGroupId);
                            jobEvent.setParamFilePath(jobInfo.getParamFilePath());
                            events.add(jobEvent);
                        }
                        jobEvents.addAll(events);
                        if (events.size() > 0) {
                            tempEvents.add(events.get(0));
                        }
                    }
                }
            }
            parentEvents = tempEvents;
        }
        if (jobEvents.size() > 0) {
			jobEventMapper.batchInsert(jobEvents);
		}
		// 更新job中的执行时间
        try {
            CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(getTriggerKey(jobInfo.getId(), jobInfo.getCreateTime().getTime()));
            updateRunningStatus(jobInfo.getId(), JobScheduler.RunningStatus.RUNNING, cronTrigger.getPreviousFireTime(), cronTrigger.getNextFireTime());
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * Add job event
     * @author clock
     * @date 2018/12/4 下午3:23
     * @param jobId job id
     * @return add result
     */
	public static boolean addJobEvent(Long jobId) {
        JobInfo jobInfo = jobInfoMapper.findJobInfoById(jobId);
        return jobInfo != null && addJobEvent(jobInfo);
    }

    /**
     * Remove all jobs
     * @throws SchedulerException
     */
    public static void removeAllJobs() throws SchedulerException {
	    scheduler.clear();
	    ClusterPeerNodes.getSingleton().clear();
    }

}
