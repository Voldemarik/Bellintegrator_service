package ru.bellintegrator.users_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import ru.bellintegrator.users_service.model.UserDto;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    private final ObjectMapper objectMapper;

    public RedisConfig(ObjectMapper objectMapper) {
        objectMapper.registerModule(new SpringDataJacksonConfiguration.PageModule(null));
        this.objectMapper = objectMapper;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        Jackson2JsonRedisSerializer<UserDto> userDtoSerializer = new Jackson2JsonRedisSerializer<>(UserDto.class);

        RedisCacheConfiguration userCacheConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)).disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(userDtoSerializer));

        JavaType pageUserDtoType = objectMapper.getTypeFactory().constructParametricType(Page.class, UserDto.class);
        Jackson2JsonRedisSerializer<Page<UserDto>> pageSerializer = new Jackson2JsonRedisSerializer<>(objectMapper ,pageUserDtoType);

        RedisCacheConfiguration usersPageCacheConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)).disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(pageSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("user", userCacheConfig);
        cacheConfigurations.put("users", usersPageCacheConfig);

        return RedisCacheManager.builder(redisConnectionFactory)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}