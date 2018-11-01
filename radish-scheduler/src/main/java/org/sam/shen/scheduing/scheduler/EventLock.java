package org.sam.shen.scheduing.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * 任务事件锁<br>
 * 抢占任务时, 对已抢占到的任务
 *
 * @author suoyao
 * @date 2018年8月27日 下午4:39:04
 */
public class EventLock {
    private static Logger logger = LoggerFactory.getLogger(EventLock.class);

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Lock key path.
     */
    private String lockKey;

    private long expireTime = 30L;

    private volatile boolean locked = false;

    public EventLock(RedisTemplate<String, Object> redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey + "_lock";
    }

    public EventLock(RedisTemplate<String, Object> redisTemplate, String lockKey, long expireTime) {
        this(redisTemplate, lockKey);
        this.expireTime = expireTime;
    }

    public String getLockKey() {
        return lockKey;
    }

    private boolean setNX(final String key, final String value) {
        Object obj = null;
        try {
            obj = redisTemplate.execute((RedisCallback<Object>) connection -> {
                StringRedisSerializer serializer = new StringRedisSerializer();
//					Boolean success = connection.setNX(serializer.serialize(key), serializer.serialize(value));
                // 通过jedisCluster.set(final String key, final String value, final String nxxx, final String expx, final long time)实现
                Boolean success = connection.set(serializer.serialize(key), serializer.serialize(value),
                        Expiration.seconds(expireTime), RedisStringCommands.SetOption.SET_IF_ABSENT);
                connection.close();
                return success;
            });
        } catch (Exception e) {
            logger.error("setNX redis error, key : {}", key);
        }
        return obj != null ? (Boolean) obj : false;
    }

    /**
     * 获得 lock.
     * 通过setnx尝试设置某个key的值,成功(当前没有这个锁)则返回,成功获得锁
     *
     * @return
     * @author suoyao
     * @date 下午5:12:17
     */
    public synchronized boolean lock() {
        if (this.setNX(lockKey, String.valueOf(expireTime))) {
            // lock acquired
            locked = true;
        }
        return locked;
    }

    /**
     * Acqurired lock release.
     */
    public synchronized void unlock() {
        if (locked) {
            redisTemplate.delete(lockKey);
            locked = false;
        }
    }

}
