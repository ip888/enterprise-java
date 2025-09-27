package com.enterprise.portfolio.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }
    
    @Bean
    @Primary
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
            .<String, Object>newSerializationContext(new StringRedisSerializer())
            .hashKey(new StringRedisSerializer())
            .hashValue(new GenericJackson2JsonRedisSerializer())
            .value(new GenericJackson2JsonRedisSerializer())
            .build();
            
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
    
    @Bean 
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
            .<String, String>newSerializationContext(new StringRedisSerializer())
            .value(new StringRedisSerializer())
            .hashKey(new StringRedisSerializer())
            .hashValue(new StringRedisSerializer())
            .build();
            
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}