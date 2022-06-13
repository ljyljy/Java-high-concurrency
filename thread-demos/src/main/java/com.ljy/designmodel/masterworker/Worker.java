package com.ljy.designmodel.masterworker;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Worker<T extends Task, R> {
    // 接收任务的阻塞队列
    private LinkedBlockingQueue<T> taskQueue = new LinkedBlockingQueue<>();
    //worker 的编号
    static AtomicInteger workerIdx = new AtomicInteger(1);
    private int workerId;
    //执行任务的线程
    private Thread thread = null;

    public Worker() {
        this.workerId = workerIdx.getAndIncrement();
        thread = new Thread(() -> this.runThread());
        thread.start();
    }

    /**
     * 轮询执行具体的任务（求和）
     */
    public void runThread() {
        // 轮询启动所有的子任务
        for (; ; ) {
            try {
                //从阻塞队列中提取任务
                T task = this.taskQueue.take();
                task.setWorkerId(workerId);
                // 执行具体任务(doExec-执行累加（局部/分治）)，并更改task自身的一些属性（如子结果subRes）
                // Consumer型：无返回值
                task.executeTask();  // ❤❤❤ 理解！
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //接收任务到异步队列（ljyljy: 应该是任务完成后，提交任务吧？）
    public void submitTaskToMaster(T task, Consumer<R> action) {
        task.resultAction = action; // R: 子任务的累加结果
        try {
            this.taskQueue.put(task);  // 放回任务池（回收）
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
