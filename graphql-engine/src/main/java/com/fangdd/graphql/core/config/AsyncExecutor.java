package com.fangdd.graphql.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * 异步任务
 *
 * @author xuwenzhen
 */
@Configuration
@EnableAsync
@ConfigurationProperties("executor.default")
public class AsyncExecutor implements AsyncConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(AsyncExecutor.class);

    private int queueSize = 10000;

    private int coreSize = 0;

    private int maxSize = 100;

    private int keepAliveTime = 10;

    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);

    private Executor executor;


    @Override
    public Executor getAsyncExecutor() {
        if (executor != null) {
            return executor;
        }
        if (coreSize <= 0) {
            coreSize = Runtime.getRuntime().availableProcessors();
        }

        logger.info("coreSize={}, maxSize={}, keepAliveTime={}, queueSize={}", coreSize, maxSize, keepAliveTime, queueSize);
        executor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                queue,
                Thread::new,
                new ThreadPoolExecutor.DiscardPolicy()
        );
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> logger.error("async error, method: {}", method.getName(), ex);
    }

    @PreDestroy
    public void preDestroy() {
        while (queue.peek() != null) {
            logger.info("default executor queue size: {}", queue.size());
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                logger.error("Thread.sleep(1);发生错误", e);
            }
        }

        logger.info("default executor queue is clear!");
    }
}
