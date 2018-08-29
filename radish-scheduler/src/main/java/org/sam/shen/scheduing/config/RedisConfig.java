package org.sam.shen.scheduing.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig extends CachingConfigurerSupport {
	
	@Bean
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(factory);
		// key序列化方式;（不然会出现乱码;）,但是如果方法上有Long等非String类型的话，会报类型转换错误；
		// 所以在没有自己定义key生成策略的时候，以下这个代码建议不要这么写，可以不配置或者自己实现ObjectRedisSerializer
		// 或者JdkSerializationRedisSerializer序列化方式;
		RedisSerializer<String> redisSerializer = new StringRedisSerializer();// Long类型不可以会出现异常信息;
		redisTemplate.setKeySerializer(redisSerializer);
		redisTemplate.setValueSerializer(redisSerializer);
		redisTemplate.setHashKeySerializer(redisSerializer);
		redisTemplate.setHashValueSerializer(redisSerializer);
		return redisTemplate;
		// JdkSerializationRedisSerializer序列化方式;
		/*JdkSerializationRedisSerializer jdkRedisSerializer = new JdkSerializationRedisSerializer();
		redisTemplate.setKeySerializer(jdkRedisSerializer);
		redisTemplate.setValueSerializer(jdkRedisSerializer);
		redisTemplate.setHashValueSerializer(jdkRedisSerializer);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;*/
	}
}
