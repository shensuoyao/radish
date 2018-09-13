package org.sam.shen.scheduing.scheduler;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

/**
 *  执行Job Callback的bean
 * @author suoyao
 * @date 2018年8月16日 下午2:45:49
  * 
 */
public class EventJobBean extends QuartzJobBean {
	private static final Logger logger = LoggerFactory.getLogger(EventJobBean.class);

	private Long jobId;
	
	public EventJobBean() {
		super();
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		init(context);
		// JobInfo jobInfo = (JobInfo) dataMap.get("jobInfo");
		// 1. load JobInfo
		JobInfo jobInfo = RadishDynamicScheduler.jobInfoMapper.findJobInfoById(jobId);
		// 2. 检查JobInfo的enable状态是否为启用
		if(jobInfo.getEnable() != 1) {
			if(logger.isInfoEnabled()) {
				logger.info("job is disenabled {}", jobInfo.getJobName());
			}
			// 禁用状态, 则从调度器中删除该任务的调度
			try {
				RadishDynamicScheduler.removeJob(jobInfo.getId(), jobInfo.getJobName());
			} catch (SchedulerException e) {
				logger.error("remove job [" + jobInfo.getJobName() + "] from scheduler failed", e);
			}
		} else {
			// 3. 发送Event事件到抢占任务事件队列
			List<String> agentHandlers = Splitter.onPattern(",|-").splitToList(jobInfo.getExecutorHandlers());
			Map<String, Object> eventHash = Maps.newHashMap();
			Stream.iterate(0, i -> i + 1).limit(agentHandlers.size()).forEach(i -> {
				if(i % 2 == 0) {
					if(eventHash.containsKey(agentHandlers.get(i))) {
						String val = String.valueOf(eventHash.get(agentHandlers.get(i))).concat(",").concat(agentHandlers.get(i + 1));
						eventHash.put(agentHandlers.get(i), val);
					} else {
						eventHash.put(agentHandlers.get(i), agentHandlers.get(i + 1));
					}
				}
			});
			JobEvent jobEvent = new JobEvent(jobInfo.getId(), jobInfo.getExecutorHandlers(), jobInfo.getHandlerType(),
			        EventStatus.READY, jobInfo.getPriority(), jobInfo.getCmd(), jobInfo.getParams());
			jobEvent.setParentJobId(jobInfo.getParentJobId());
			RadishDynamicScheduler.jobEventMapper.saveJobEvent(jobEvent);
			
			RadishDynamicScheduler.redisService.hmset(Constant.REDIS_EVENT_PREFIX.concat(jobEvent.getEventId()),
			        eventHash);
		}
		destory();
	}
	
	public void init(JobExecutionContext context) {
		context.getMergedJobDataMap();
		if (logger.isInfoEnabled()) {
			logger.info("init job ==== {} =====", jobId);
		}
	}

	public void destory() {
		if(logger.isInfoEnabled()) {
			logger.info("destory job ==== {} =====", jobId);
		}
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
}
