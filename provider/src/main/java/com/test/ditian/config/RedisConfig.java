//package com.test.ditian.config;
//
//import lombok.val;
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//import org.redisson.spring.data.connection.RedissonConnectionFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.CacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean(name = {"redisTemplate", "stringRedisTemplate"})
//    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
//        StringRedisTemplate redisTemplate = new StringRedisTemplate();
//        redisTemplate.setConnectionFactory(factory);
//        return redisTemplate;
//    }
//
//    @Bean
//    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
//        return new RedissonConnectionFactory(redisson);
//    }
//
//    @Bean
//    public StringRedisSerializer stringRedisSerializer() {
//        return new StringRedisSerializer();
//    }
//
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//        val expiration = Duration.ofSeconds(600);
//        return RedisCacheManager.builder(redisConnectionFactory)
//                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(expiration)).build();
//    }
//
//}
