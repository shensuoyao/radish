package org.sam.shen.scheduing.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.log.LogReader;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.AgentMapper;
import org.sam.shen.scheduing.mapper.JobEventMapper;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.EventLock;
import org.sam.shen.scheduing.sendcloud.SendEmailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
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
	private AgentMapper agentMapper;
	
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	private RedisService redisService;

	/**
	 *  触发获取可执行的任务
	 * @author suoyao
	 * @date 下午4:53:30
	 * @param agentId
	 * @return
	 */
	@Transactional
	public HandlerEvent triggerJobEvent(Long agentId) {
		// 获取所有事件key
		Set<String> keys = redisService.getKeys(Constant.REDIS_EVENT_PREFIX.concat("*"));
		
		// 获取所有Event 的优先级
		Map<String, Integer> eventKeys = Maps.newHashMap();
		for(String key : keys) {
			eventKeys.put(key, (Integer) redisService.hget(key, "priority"));
		}
		
		// 根据优先级排序
		// 优先级倒序排列
		List<Map.Entry<String, Integer>> list = Lists.newArrayList(eventKeys.entrySet()); // new ArrayList<>(eventKeys.entrySet());
		list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
		// 按照优先级从高到低抢占
		for(Map.Entry<String, Integer> mapping: list) {
			if(redisService.hkeyExists(mapping.getKey(), String.valueOf(agentId))) {
				// 可以抢占
				EventLock lock = new EventLock(redisTemplate, String.valueOf(mapping.getKey()), String.valueOf(agentId));
				try {
					if(lock.lock()) {
						JobEvent event = jobEventMapper
						        .findJobEventByEventId(mapping.getKey().substring(Constant.REDIS_EVENT_PREFIX.length()));
						if(null == event) {
							continue;
						}
						
						String handlers = (String) redisService.hget(mapping.getKey(), String.valueOf(agentId));
						
						// 获得锁成功
						event.setStat(EventStatus.HANDLE);
						event.setHandlerAgentId(agentId);
						jobEventMapper.upgradeJobEvent(event);
						redisService.delete(mapping.getKey());
						HandlerEvent handlerEvent = new HandlerEvent(event.getEventId(),
						        String.valueOf(event.getJobId()), handlers, event.getCmd(), event.getHandlerType());
						if(StringUtils.isNotEmpty(event.getParams())) {
							handlerEvent.setParams(event.getParams().split(System.lineSeparator()));
						}
						return handlerEvent;
					}
				} catch (Exception e) {
					logger.error("event lock error.", e);
				} finally {
					lock.unlock();
				}
			}
		}
		return new HandlerEvent();
	}
	
	/**
	 * 处理 Event执行结果
	 * @author suoyao
	 * @date 下午5:00:19
	 * @param eventId
	 * @param resp
	 */
	@Transactional
	public void handlerJobEventReport(String eventId, Resp<String> resp) {
		
		JobEvent jobEvent = jobEventMapper.findJobEventByEventId(eventId);
		if(null == jobEvent) {
			return;
		}
		
		if(resp.getCode() == Resp.SUCCESS.getCode()) {
			// 执行成功, event 事件的状态改为成功
			jobEvent.setStat(EventStatus.SUCCESS);
			jobEventMapper.upgradeJobEvent(jobEvent);
		} else {
			// 执行失败, 判断任务的失败策略. 
			JobInfo jobInfo = jobInfoMapper.findJobInfoById(jobEvent.getJobId());
			if(null == jobInfo) {
				return;
			}
			jobEvent.setStat(EventStatus.FAIL);
			if (jobInfo.getHandlerFailStrategy().equals(HandlerFailStrategy.RETRY)) {
				// 重试, 则增加重试次数, 并且更新重试状态
				jobEvent.setStat(EventStatus.RETRY);
				jobEvent.setRetryCount(jobEvent.getRetryCount() + 1);
			}
			if(jobInfo.getHandlerFailStrategy().equals(HandlerFailStrategy.ALARM)) {
				// 发送告警邮件或者短信
				if(StringUtils.isNotEmpty(jobInfo.getAdminEmail())) {
					try {
						SendEmailClient.sendEmail(jobInfo.getAdminEmail(), "Radish Handler Fail Alarm", jobEvent.toString(),  null);
					} catch (Exception e) {
						logger.error("send alarm mail fail.", e);
					}
				}
			}
			if(jobInfo.getHandlerFailStrategy().equals(HandlerFailStrategy.DISCARD)) {
				// 丢弃, 则直接更新状态, 什么也不做
			}
			jobEventMapper.upgradeJobEvent(jobEvent);
		}
	}
	
	public Page<JobEvent> queryJobEventForPager(int index, int limit, EventStatus stat) {
		PageHelper.startPage(index, limit);
		return jobEventMapper.queryJobEventForPager(stat.name());
	}
	
	/**
	 *  读取事件日志
	 * @author suoyao
	 * @date 下午3:00:48
	 * @param eventId
	 * @param agentId
	 * @return
	 */
	public LogReader readEventLogFromAgent(String eventId, Long agentId) {
		Agent agent = agentMapper.findAgentById(agentId);
		String logUrl = Constant.HTTP_PREFIX.concat(agent.getAgentIp()).concat(":")
		        .concat(String.valueOf(agent.getAgentPort())).concat(Constant.AGENT_CONTEXT_PATH);
		try {
			Resp<LogReader> resp = RestRequest.getUriVariables(logUrl, LogReader.class, eventId);
			if(resp.getCode() == Resp.SUCCESS.getCode()) {
				return resp.getData();
			}
		} catch (Exception e) {
			logger.error("read event log fail.", e);
			return new LogReader(Arrays.asList("客户端未开启日志获取"));
		}
		return null;
	}
	
}
