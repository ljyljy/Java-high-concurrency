package com.ljy.threadpool;

import com.ljy.util.ShutdownHookThread;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.ljy.util.ThreadUtil.*;
//懒汉式单例创建线程池：用于CPU密集型任务

@Slf4j

public class CpuIntenseTargetThreadPoolLazyHolder {
    //线程池： 用于CPU密集型任务
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            MAXIMUM_POOL_SIZE, // == CPU_COUNT = Runtime.getRuntime().availableProcessors();
            MAXIMUM_POOL_SIZE, // CPU_COUNT
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue(QUEUE_SIZE), // 有界：10000
            new CustomThreadFactory("cpu"));


    public static ThreadPoolExecutor getInnerExecutor() {
        return EXECUTOR;
    }

    static {
        log.info("线程池已经初始化");

        // 将包括“核心线程”在内的，没有任务分配的任何线程，在等待keepAliveTime时间后全部进行回收：
        EXECUTOR.allowCoreThreadTimeOut(true);
        //JVM关闭时的钩子函数
        Runtime.getRuntime().addShutdownHook(
                new ShutdownHookThread("CPU密集型任务线程池", new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        //优雅关闭线程池
                        shutdownThreadPoolGracefully(EXECUTOR);
                        return null;
                    }
                }));
    }
}
