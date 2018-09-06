package org.sam.shen.scheduing.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

@Service
public class RedisService {
	
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	public boolean set(final String key, Object value) {
		boolean result = false;
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * @author suoyao
	 * @date 下午3:44:06
	 * @param key
	 * @param value
	 * @param expireTime 失效时间 毫秒
	 * @return
	 */
	public boolean set(final String key, Object value, Long expireTime) {
		boolean result = false;
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 *   删除key
	 * @author suoyao
	 * @date 下午3:45:29
	 * @param keys
	 */
	public void remove(final String... keys) {
		for (String key : keys) {
			remove(key);
		}
	}
	
	public boolean exists(final String key) {
		return redisTemplate.hasKey(key);
	}
	
	public Object get(final String key) {
		Object result = null;
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		result = operations.get(key);
		return result;
	}
	
	public void hset(String key, String hashKey, Object value) {
		redisTemplate.opsForHash().put(key, hashKey, value);
	}
	
	public void hmset(String key, Map<String, Object> m) {
		redisTemplate.opsForHash().putAll(key, m);
	}
	
	/**
	 *  设置Hash过期时间
	 * @author suoyao
	 * @date 上午10:39:20
	 * @param key
	 * @param hashKey
	 * @param value
	 * @param timeout
	 *    过期时间为 秒 
	 */
	public void hsetEx(String key, String hashKey, Object value, long timeout) {
		redisTemplate.opsForHash().put(key, hashKey, value);
		redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
	}
	
	public void hmsetEx(String key, Map<String, Object> m, long timeout) {
		redisTemplate.opsForHash().putAll(key, m);
		redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
	}
	
	public Set<String> getKeys(String pattern) {
		return redisTemplate.keys(pattern);
	}
	
	public Map<String, Object> hmget(String key) {
		Set<Object> hashKeys = redisTemplate.opsForHash().keys(key);
		if(null == hashKeys || hashKeys.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Object> hash = Maps.newHashMap();
		hashKeys.forEach(hashKey -> hash.put(String.valueOf(hashKey), redisTemplate.opsForHash().get(key, hashKey)));
		return hash;
	}
	
}
