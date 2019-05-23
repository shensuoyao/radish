package org.sam.shen.agent.service;

import org.sam.shen.agent.entity.ExpiredEvent;
import org.sam.shen.agent.mapper.ExpiredEventMapper;
import org.sam.shen.core.constants.Constant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author clock
 * @date 2019-05-23 09:10
 */
@Service
public class ExpiredEventService {

    @Resource
    private ExpiredEventMapper expiredEventMapper;

    private final RedisTemplate<String, ?> redisTemplate;

    public ExpiredEventService(RedisTemplate<String, ?> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询过期event
     * @author clock
     * @date 2019-05-23 09:14
     * @return 过期的event
     */
    public List<ExpiredEvent> getExpiredEvent() {
        return expiredEventMapper.selectExpiredEvent();
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
        expiredEventMapper.deleteExpiredEvent(eventIds);
        // 删除redis缓存数据
        List<String> eventKeys = eventIds.stream().map(eventId -> Constant.REDIS_EVENT_PREFIX + eventId).collect(Collectors.toList());
        redisTemplate.delete(eventKeys);
    }

}
