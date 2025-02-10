package org.example.user.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // 빈을 통해서 레디스 연동 구성

    // 레디스와 연동(결) 관리하는 객체를 빈으로 구성
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 연결 클라이언트 객체를 생성 반환
        // (*)LettuceConnectionFactory, JedisConnectionFactory
        // 환경 설정 정보에 있는 내용을 기반으로 연결
        return new LettuceConnectionFactory();
    }

    // 레디스 데이터를 처리하는 객체를 빈으로 구성
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory()); // 연결설정
        // 설정
        template.setKeySerializer(new StringRedisSerializer()); // 키를 저정할때 문자열 객체 직렬화를 통해서 처리
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 값 객체 직렬화로 처리
        return template;
    }
}
