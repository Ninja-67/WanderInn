package com.coding.projects.WanderInn.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String INVENTORY_BY_ROOM = "inventoryByRoom";
    public static final String INVENTORY_SEARCH = "inventorySearch";

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        var cm = new CaffeineCacheManager(INVENTORY_BY_ROOM, INVENTORY_SEARCH);
        cm.setCaffeine(caffeine);
        return cm;
    }
}
