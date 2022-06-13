package com.ljy.visiable;

import com.ljy.util.Print;
import com.ljy.util.ThreadUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

class Driver {
    private static final int N = 100; // 乘客数
    private static AtomicInteger cnt = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch doneSignal = new CountDownLatch(N);
        //取得CPU密集型线程池
        Executor e = ThreadUtil.getCpuIntenseTargetThreadPool();

        for (int i = 1; i <= N; ++i) // 启动报数任务
            e.execute(new Person(doneSignal, i));

        doneSignal.await(); //等待报数完成
        Print.tcfo("上车总人数：" + cnt.intValue());
        Print.tcfo("人数到齐，开车");

    }

    static class Person implements Runnable {
        private final CountDownLatch doneSignal;
        private final int i;

        Person(CountDownLatch doneSignal, int i) {
            this.doneSignal = doneSignal;
            this.i = i;
        }

        public void run() {
            try {
                //报数
                Print.tcfo("第" + i + "个人已到");
                doneSignal.countDown();
                int tmpCnt = cnt.incrementAndGet();
            } catch (Exception ex) {
            } // return;
        }


    }
}

