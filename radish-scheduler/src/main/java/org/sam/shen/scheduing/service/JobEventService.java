package org.sam.shen.scheduing.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.JobEventMapper;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.EventLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service("jobEventService")
public class JobEventService {
	
	private Logger logger = LoggerFactory.getLogger(JobEventService.class);
	
	@Resource
	private JobEventMapper jobEventMapper;
	
	@Resource
	private JobInfoMapper jobInfoMapper;
	
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	/**
	 *  触发获取可执行的任务
	 * @author suoyao
	 * @date 下午4:53:30
	 * @param agentId
	 * @return
	 */
	@Transactional
	public JobEvent triggerJobEvent(Long agentId) {
		List<JobEvent> triggerEvent = jobEventMapper.queryJobEventByAgentId(agentId);
		if(null == triggerEvent || triggerEvent.isEmpty()) {
			return null;
		}
		for (JobEvent event : triggerEvent) {
			if(!event.getStat().equals(EventStatus.READY) && !event.getStat().equals(EventStatus.RETRY)) {
				continue;
			}
			EventLock lock = new EventLock(redisTemplate, String.valueOf(event.getEventId()));
			try {
				lock.lock();
				if(StringUtils.isNotEmpty(event.getParentJobId())) {
					// 获取父任务数量
					List<Long> ids = Lists.newArrayList();
					Arrays.asList(event.getParentJobId().split(",")).forEach(id -> ids.add(Long.valueOf(id)));
					Integer count = jobEventMapper.countJobEventInJobIds(ids);
					if(count > 0) {
						continue;
					}
					// 删除与event关联的其他的Agent事件
					Map<String, Object> param = Maps.newHashMap();
					param.put("eventId", event.getEventId());
					param.put("agentId", event.getAgentId());
					jobEventMapper.deleteJobEventNotEqual(param);
					// 将 Event 更新为处理中
					param.put("stat", EventStatus.HANDLE);
					jobEventMapper.upgradeJobEventStatus(param);
				}
			} catch (InterruptedException e) {
				logger.error("event lock error.", e);
			} finally {
				lock.unlock();
			}
			return event;
		}
		return null;
	}
	
	/**
	 * 处理 Event执行结果
	 * @author suoyao
	 * @date 下午5:00:19
	 * @param eventId
	 * @param resp
	 */
	@Transactional
	public void handlerJobEventReport(Long eventId, Resp<String> resp) {
		Map<String, Object> param = Maps.newHashMap();
		param.put("eventId", eventId);
		if(resp.getCode() == Resp.SUCCESS.getCode()) {
			// 执行成功, event 事件的状态改为成功
			param.put("stat", EventStatus.SUCCESS);
			jobEventMapper.upgradeJobEventStatus(param);
		} else {
			// 执行失败, 判断任务的失败策略. 
			List<JobEvent> handlerEvent = jobEventMapper.queryJobEventByEventId(eventId);
			if(null == handlerEvent || handlerEvent.isEmpty()) {
				return;
			}
			JobInfo jobInfo = jobInfoMapper.findJobInfoById(handlerEvent.get(0).getJobId());
			if(null == jobInfo) {
				return;
			}
			param.put("stat", EventStatus.FAIL);
			if (jobInfo.getHandlerFailStrategy().equals(HandlerFailStrategy.RETRY)) {
				// 重试, 则增加重试次数, 并且更新重试状态
				param.put("stat", EventStatus.RETRY);
				param.put("retryCount", new Integer(handlerEvent.get(0).getRetryCount() + 1));
			}
			if(jobInfo.getHandlerFailStrategy().equals(HandlerFailStrategy.ALARM)) {
				// 发送告警邮件或者短信
				// TODO
			}
			if(jobInfo.getHandlerFailStrategy().equals(HandlerFailStrategy.DISCARD)) {
				// 丢弃, 则直接更新状态, 什么也不做
			}
			jobEventMapper.upgradeJobEventStatus(param);
		}
	}
	
}
