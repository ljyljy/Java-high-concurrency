package com.ljy.completableFutureDemo;

import com.ljy.util.Print;
import com.ljy.util.ThreadUtil;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.ljy.util.ThreadUtil.sleepSeconds;

public class CompletableFutureDemo {
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Test
    public void thenApply() throws Exception {

        CompletableFuture cf = CompletableFuture.supplyAsync(() -> {
            try {
                //休眠2秒
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Print.tco("supplyAsync " + Thread.currentThread().getName());
            return "【hello】";
        }, executorService).thenAccept(s -> {
            try {
                Print.tco("thenApply_test" + s + " world ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Print.tco(Thread.currentThread().getName());
        while (true) {
            if (cf.isDone()) {
                Print.tco("CompletedFuture...isDown");
                break;
            }
        }
    }

    //无返回值异步调用
    @Test
    public void runAsyncDemo() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
        {
            sleepSeconds(1);//模拟执行1秒
            Print.tco("run end ...");
        });

        //等待异步任务执行完成,现时等待2秒
        future.get(2, TimeUnit.SECONDS);
    }

    //有返回值异步调用
    @Test
    public void supplyAsyncDemo() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            sleepSeconds(1);//模拟执行1秒
            Print.tco("run end ...");
            return System.currentTimeMillis() - start;
        });

        //等待异步任务执行完成, 限时等待5秒
        long time = future.get(5, TimeUnit.SECONDS);
        Print.tco("异步执行耗时（秒） = " + time / 1000);
    }

    @Test
    public void whenCompleteDemo() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            sleepSeconds(1);//模拟执行1秒
            Print.tco("抛出异常！");
            throw new RuntimeException("发生异常");
            //Print.tco("run end ...");
        });

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
                Print.tco("runAsync done");
            } catch (InterruptedException e) {
                throw new RuntimeException("发生异常" + e);
            }
        });

        //设置执行完成后的回调钩子 （↓ 写法1）
        future.whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void t, Throwable action) {
                Print.tco("执行完成！");
            }
        });
//        future.whenComplete((t, action) -> Print.tco("执行完成！")); // 写法2

        //设置发生异常后的回调钩子
        future.exceptionally(new Function<Throwable, Void>() {
            @Override
            public Void apply(Throwable t) {
                Print.tco("执行失败！" + t.getMessage());
                return null;
            }
        });

        future.get();
    }

    @Test
    public void handleDemo() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            sleepSeconds(1);//模拟执行1秒
            Print.tco("抛出异常！");
            throw new RuntimeException("发生异常");
            //Print.tco("run end ...");
        });

        //设置执行完成后的回调钩子
        future.handle(new BiFunction<Void, Throwable, Void>() {
            @Override
            public Void apply(Void input, Throwable throwable) {
                if (throwable == null) {
                    Print.tcfo("没有发生异常！");
                } else {
                    Print.tcfo("sorry, 发生了异常！");
                }
                return null;
            }
        });

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            sleepSeconds(1);
            Print.tco("future2 - runAsync done！");
        });

        future2.handle((BiFunction<Void, Throwable, Void>)(input, throwable) -> {
            if (throwable == null) {
                Print.tcfo("future2 - 没有发生异常！");
            } else {
                Print.tcfo("future2 - sorry, 发生了异常！");
            }
            return null;
        });

        future.get();
        future2.get();
    }

    //有返回值异步调用
    @Test
    public void threadPoolDemo() throws Exception {
        //业务线程池
        ThreadPoolExecutor pool = ThreadUtil.getMixedTargetThreadPool();
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() ->
        {
            Print.tco("run begin ...");
            long start = System.currentTimeMillis();
            sleepSeconds(1);//模拟执行1秒
            Print.tco("run end ...");
            return System.currentTimeMillis() - start;
        }, pool);

        //等待异步任务执行完成, 限时等待2秒
        long time = future.get(2, TimeUnit.SECONDS);
        Print.tco("异步执行耗时（秒） = " + time / 1000);
    }

    @Test
    public void thenApplyDemo() throws Exception {
        // 写法1
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            long firstStep = 10L + 10L;
            Print.tco("firstStep outcome is " + firstStep);
            return firstStep;
        }).thenApplyAsync(firstStepOutCome -> {
            long secondStep = firstStepOutCome * 2;
            Print.tco("secondStep outcome is " + secondStep);
            return secondStep;
        });

        // 写法2
        CompletableFuture<Long> future2 = CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                long firstStep = 10L + 10L;
                Print.tco("firstStep outcome is " + firstStep);

                return firstStep;
            }
        }).thenApplyAsync(new Function<Long, Long>() {
            @Override
            public Long apply(Long firstStepOutCome) {
                long secondStep = firstStepOutCome * 3;
                Print.tco("secondStep outcome is " + secondStep);
                return secondStep;
            }
        });

        long result = future.get();
        Print.tco(" future is " + future);
        Print.tco(" outcome is " + result);
    }

    @Test
    public void thenComposeDemo() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            long firstStep = 10L + 10L;
            Print.tco("firstStep outcome is " + firstStep);
            return firstStep;
        }).thenCompose(firstStepOutCome -> CompletableFuture.supplyAsync(() -> {
            long secondStep = firstStepOutCome * 2;
            Print.tco("secondStep outcome is " + secondStep);
            return secondStep;
        }));

        long result = future.get();
        Print.tco(" outcome is " + result);


        // 写法2
        CompletableFuture<Long> future2 = CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                long firstStep = 10L + 10L;
                Print.tco("firstStep2 outcome is " + firstStep);

                return firstStep;
            }
        }).thenCompose(new Function<Long, CompletionStage<Long>>() {
            @Override
            public CompletionStage<Long> apply(Long firstStepOutCome) {
                return CompletableFuture.supplyAsync(new Supplier<Long>() {
                    @Override
                    public Long get() {
                        long secondStep = firstStepOutCome * 3;
                        Print.tco("secondStep2 outcome is " + secondStep);
                        return secondStep;
                    }
                });
            }
        });

        long result2 = future2.get();
        Print.tco(" outcome2 is " + result2);
    }

    @Test
    public void thenCombineDemo() throws Exception {
        CompletableFuture<Integer> future1 =
                CompletableFuture.supplyAsync(() -> {
                    Integer firstStep = 10 + 10;
                    Print.tco("firstStep outcome is " + firstStep);
                    return firstStep;
                });
        CompletableFuture<Integer> future2 =
                CompletableFuture.supplyAsync(new Supplier<Integer>() {
                    @Override
                    public Integer get() {
                        Integer secondStep = 10 + 10;
                        Print.tco("secondStep outcome is " + secondStep);
                        return secondStep;
                    }
                });
        CompletableFuture<Integer> future3 = future1.thenCombine(future2,
                (step1OutCome, step2OutCome) -> step1OutCome * step2OutCome);

//        // 写法2
//        CompletableFuture<Integer> future3_v2 = future1.thenCombine(future2,
//                new BiFunction<Integer, Integer, Integer>() {
//                    @Override
//                    public Integer apply(Integer step1OutCome, Integer step2OutCome) {
//                        return step1OutCome * step2OutCome;
//                    }
//                });

        Integer result = future3.get();
        Print.tco(" outcome is " + result);
    }

    @Test
    public void applyToEitherDemo() throws Exception {
        CompletableFuture<Integer> future1 =
                CompletableFuture.supplyAsync(new Supplier<Integer>() {
                    @Override
                    public Integer get() {
                        Integer firstStep = 10 + 10;
                        Print.tco("firstStep outcome is " + firstStep);
                        return firstStep;
                    }
                });
        CompletableFuture<Integer> future2 =
                CompletableFuture.supplyAsync(new Supplier<Integer>() {
                    @Override
                    public Integer get() {
                        Integer secondStep = 100 + 100;
                        Print.tco("secondStep outcome is " + secondStep);
                        return secondStep;
                    }
                });
        CompletableFuture<Integer> future3 = future1.applyToEither(future2,
                new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer eitherOutCome) {
                        return eitherOutCome;
                    }
                });
        Integer result = future3.get();
        Print.tco(" outcome is " + result);
    }

    @Test
    public void allOfDemo() throws Exception {
        CompletableFuture<Void> future1 =
                CompletableFuture.runAsync(() -> Print.tco("模拟异步任务1"));

        CompletableFuture<Void> future2 =
                CompletableFuture.runAsync(() -> Print.tco("模拟异步任务2"));
        CompletableFuture<Void> future3 =
                CompletableFuture.runAsync(() -> Print.tco("模拟异步任务3"));
        CompletableFuture<Void> future4 =
                CompletableFuture.runAsync(() -> Print.tco("模拟异步任务4"));

        CompletableFuture<Void> all =
                CompletableFuture.allOf(future1, future2, future3, future4);
        all.join();
    }

}
