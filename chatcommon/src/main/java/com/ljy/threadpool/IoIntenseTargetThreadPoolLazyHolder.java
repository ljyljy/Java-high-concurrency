package com.ljy.threadpool;

import com.ljy.util.ShutdownHookThread;
import com.ljy.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static com.ljy.util.ThreadUtil.*;

@Slf4j
//懒汉式单例创建线程池：用于IO密集型任务
public class IoIntenseTargetThreadPoolLazyHolder {
    //线程池： 用于IO密集型任务（线程数=CPU数*2）
    public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            IO_MAX, // Math.max(2, CPU_COUNT * 2)
            IO_MAX,
            KEEP_ALIVE_SECONDS, // 30s
            TimeUnit.SECONDS,
            new LinkedBlockingQueue(QUEUE_SIZE), // 有界10000
            new ThreadUtil.CustomThreadFactory("io"));

    public static ThreadPoolExecutor getInnerExecutor() {
        return EXECUTOR;
    }

    static {
        log.info("线程池已经初始化");

        EXECUTOR.allowCoreThreadTimeOut(true);
        //JVM关闭时的钩子函数
        Runtime.getRuntime().addShutdownHook(
                new ShutdownHookThread("IO密集型任务线程池", new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        //优雅关闭线程池
                        shutdownThreadPoolGracefully(EXECUTOR);
                        return null;
                    }
                }));
    }
}