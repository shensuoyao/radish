package org.sam.shen.scheduing.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** 
 *  任务事件锁<br>
 *  抢占任务时, 对已抢占到的任务
 * @author suoyao
 * @date 2018年8月27日 下午4:39:04
  * 
 */
public class EventLock {
	private static Logger logger = LoggerFactory.getLogger(EventLock.class);
	
	private RedisTemplate<String, Object> redisTemplate;
	
	/**
	 * Lock key path.
	 */
	private String lockKey;
	
	private int expireMsecs = 60 * 1000;
	
	private volatile boolean locked = false;
	
	public EventLock(RedisTemplate<String, Object> redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey + "_lock";
	}
	
	public EventLock(RedisTemplate<String, Object> redisTemplate, String lockKey, int expireMsecs) {
        this(redisTemplate, lockKey);
        this.expireMsecs = expireMsecs;
    }
	
	public String getLockKey() {
        return lockKey;
    }
	
	private boolean setNX(final String key, final String value) {
		Object obj = null;
		try {
			obj = redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					StringRedisSerializer serializer = new StringRedisSerializer();
					Boolean success = connection.setNX(serializer.serialize(key), serializer.serialize(value));
					connection.close();
					return success;
				}
			});
		} catch (Exception e) {
			logger.error("setNX redis error, key : {}", key);
		}
		return obj != null ? (Boolean) obj : false;
	}
	
	/**
	 *  获得 lock.
	 *  通过setnx尝试设置某个key的值,成功(当前没有这个锁)则返回,成功获得锁
	 * @author suoyao
	 * @date 下午5:12:17
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean lock() throws InterruptedException {
		long expires = System.currentTimeMillis() + expireMsecs + 1;
		String expiresStr = String.valueOf(expires); // 锁到期时间
		if (this.setNX(lockKey, expiresStr)) {
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
