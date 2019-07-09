package org.sam.shen.agent.service;

import org.sam.shen.agent.entity.ExpiredEvent;
import org.sam.shen.agent.mapper.JobEventMapper;
import org.sam.shen.core.constants.Constant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author clock
 * @date 2019-05-23 09:10
 */
@Service
public class JobEventService {

    @Resource
    private JobEventMapper jobEventMapper;

    private final RedisTemplate<String, ?> redisTemplate;

    public JobEventService(RedisTemplate<String, ?> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询过期event
     * @author clock
     * @date 2019-05-23 09:14
     * @return 过期的event
     */
    public List<ExpiredEvent> getExpiredEvent() {
        return jobEventMapper.selectExpiredEvent();
    }

    /**
     * 清除过期event
     * @author clock
     * @date 2019-05-23 09:14
     * @param eventIds 待清除的event id
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearExpiredEvent(List<String> eventIds) {
        if (eventIds == null || eventIds.size() < 1) {
            return;
        }
        // 删除数据库数据
        jobEventMapper.deleteExpiredEvent(eventIds);
        // 删除redis缓存数据
        List<String> eventKeys = eventIds.stream().map(eventId -> Constant.REDIS_EVENT_PREFIX + eventId).collect(Collectors.toList());
        redisTemplate.delete(eventKeys);
    }

    /**
     * 将已处理的event迁移到历史表
     * @author clock
     * @date 2019-05-29 11:16
     */
    @Transactional(rollbackFor = Exception.class)
    public void migrateHandledEvent(List<Map<String, Object>> events) {
        if (events == null || events.size() < 1) {
            return;
        }
        jobEventMapper.deleteHandledEvent(events.stream()
                .map(event -> String.valueOf(event.get("event_id"))).collect(Collectors.toList()));
        jobEventMapper.batchInsertEvent(events);
    }

    /**
     * 分页查询已处理的event
     * @author clock
     * @date 2019-07-09 09:44
     * @param limit 查询数量
     * @return 已处理event
     */
    public List<Map<String, Object>> limitHandledEvent(Integer limit) {
        return jobEventMapper.selectHandledEvent(limit);
    }

}
