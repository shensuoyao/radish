package org.sam.shen.scheduing.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.mapper.JobEventMapper;
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
	private RedisTemplate<String, Object> redisTemplate;

	@Transactional
	public JobEvent triggerJobEvent(Long agentId) {
		List<JobEvent> triggerEvent = jobEventMapper.queryJobInfoByAgentId(agentId);
		if(null == triggerEvent || triggerEvent.isEmpty()) {
			return null;
		}
		for (JobEvent event : triggerEvent) {
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
	
}
