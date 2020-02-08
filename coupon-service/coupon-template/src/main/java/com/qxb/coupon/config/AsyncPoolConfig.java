package com.qxb.coupon.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author qiuxiaobin
 * @date 2020年 02月 08日 18:49
 * @description
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncPoolConfig implements AsyncConfigurer {

    @Bean
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);//核心线程数
        executor.setMaxPoolSize(20);//最大线程数
        executor.setQueueCapacity(20);//队列容量
        executor.setKeepAliveSeconds(60);//空闲时最长的存活时间
        executor.setThreadNamePrefix("CouponAsync_");//名称前缀

        executor.setWaitForTasksToCompleteOnShutdown(true);//任务关闭的时候线程是否退出
        executor.setAwaitTerminationSeconds(60);//最长等待时间

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//拒绝策略
        executor.initialize();//初始化线程池
        return executor;
    }

    //异常处理
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    @SuppressWarnings("all")
    class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler{

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
            throwable.printStackTrace();
            log.error("AsyncError：{}, method：{}, Param: {}",
                    throwable.getMessage(),method.getName(), JSON.toJSONString(objects));
            //TODO 发送邮件或者短信，做进一步的处理
        }
    }
}
