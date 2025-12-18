package com.example.demo.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - minimum number of threads
        executor.setCorePoolSize(2);

        // Max pool size - maximum number of threads
        executor.setMaxPoolSize(5);

        // Queue capacity - number of tasks that can be queued
        executor.setQueueCapacity(100);

        // Thread name prefix for easy identification
        executor.setThreadNamePrefix("rating-enrichment-");

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Maximum time to wait for tasks to complete on shutdown
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        log.info("Async task executor initialized with core pool size: {}, max pool size: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }
}
