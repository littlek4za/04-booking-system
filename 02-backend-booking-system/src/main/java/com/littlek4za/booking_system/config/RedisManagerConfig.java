package com.littlek4za.booking_system.config;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RedisManagerConfig{

        // @Bean
        // public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, // connection to redis server
        //                 ObjectMapper objectMapper,
        //                 CacheErrorHandler cacheErrorHandler) {

        //         // 1. Configure the polymorphic validator for lists and records
        //         // PolymorphicTypeValidator = security rule for JSON deserialization
        //         PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        //                         .allowIfSubType("com.littlek4za.booking_system")
        //                         .allowIfSubType("java.util")
        //                         // .allowIfSubType(Object.class) // Allows Lists, Maps, arrays
        //                         // .allowIfSubType(Record.class) // Explicitly allows your DTO Records
        //                         .build();

        //         // 2. Setup serialization format for storing values in Redis
        //         // GenericJacksonJsonRedisSerializer = JacksonJSON, add in @class support so
        //         // system know what java type to deserialize afterwards,
        //         // work with complex list, map, record etc
        //         RedisSerializer<Object> serializer = GenericJacksonJsonRedisSerializer.builder()
        //                         .typePropertyName("@class")
        //                         .enableDefaultTyping(ptv) // apply the configured rule - ptv
        //                         .build();

        //         // 3. Define cache behaviors
        //         RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        //                         .entryTtl(Duration.ofMinutes(5))
        //                         .serializeKeysWith(RedisSerializationContext.SerializationPair
        //                                         .fromSerializer(new StringRedisSerializer())) // format sample - user::1
        //                         .serializeValuesWith(RedisSerializationContext.SerializationPair
        //                                         .fromSerializer(serializer));

        //         // Create explicit rule targeting your "@Cacheable(value = "events")" name
        //         Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        //         configs.put("sample", config.entryTtl(Duration.ofMinutes(15)));

        //         return RedisCacheManager.builder(connectionFactory)
        //                         .cacheDefaults(config)
        //                         .withInitialCacheConfigurations(configs) // Hooks up the "events" rule
        //                         .build();
        // }

        @Bean
        public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
                RedisTemplate<String, String> template = new RedisTemplate<>();
                template.setConnectionFactory(factory);

                // KEY = normal string
                template.setKeySerializer(new StringRedisSerializer());

                // VALUE = JSON string
                template.setValueSerializer(new StringRedisSerializer());

                template.setHashKeySerializer(new StringRedisSerializer());
                template.setHashValueSerializer(new StringRedisSerializer());

                template.afterPropertiesSet();;

                return template;
        }

        @Bean
        public CacheErrorHandler cacheErrorHandler() {
                return new CacheErrorHandler() {

                        @Override
                        public void handleCacheClearError(RuntimeException exception, Cache cache) {
                                log.error("Redis CLEAR failed", exception);
                        }

                        @Override
                        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                                log.error("Redis EVICT failed", exception);
                        }

                        @Override
                        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                                log.error("Redis GET failed", exception);
                        }

                        @Override
                        public void handleCachePutError(RuntimeException exception, Cache arg1, Object arg2,
                                        @Nullable Object arg3) {
                                log.error("Redis PUT failed", exception);
                        }

                };
        }

}
