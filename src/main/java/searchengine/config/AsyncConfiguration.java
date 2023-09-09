package searchengine.config;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Log4j2
public class AsyncConfiguration implements AsyncConfigurer, AsyncUncaughtExceptionHandler  {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("AsyncTaskThread:");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Override
    public void handleUncaughtException(@NotNull Throwable ex, @NotNull Method method, Object @NotNull ... params) {
        log.error("Exception: " + ex.getMessage());
        log.error("Method Name: " + method.getName());
        for (Object param : params) {
            log.error("Parameter value - " + param);
        }
        ex.printStackTrace();
    }
}