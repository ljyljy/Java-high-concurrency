package com.ljy.designmodel.masterworker;

import com.ljy.util.Print;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Master<T extends Task, R> {
    // 所有worker的集合 <前缀name, Worker<task, result>>
    private HashMap<String, Worker<T, R>> workersMap = new HashMap<>();


    // 任务的集合
    private LinkedBlockingQueue<T> taskQueue = new LinkedBlockingQueue<>();

    //任务处理结果集 <name, 累加结果R (Long/Integer)>
    protected Map<String, R> resultMap = new ConcurrentHashMap<>();

    //Master的任务调度线程
    private Thread thread = null;

    //保持最终的和, 最终子任务回调的和（归并求和）
    private AtomicLong sumMerged = new AtomicLong(0);

    public Master(int workerCount) {
        // 每个worker对象都需要持有queue的引用, 用于领任务与提交结果
        for (int i = 0; i < workerCount; i++) {
            Worker<T, R> worker = new Worker<>();
            workersMap.put("子节点: " + i, worker);
        }
        // 分治 - 每个Master进行调度 - 分配task，让task执行加法。
        thread = new Thread(() -> this.executeThread());
        thread.start();
    }

    // 提交任务
    public void addTask(T task) {
        taskQueue.add(task);
    }


    //结果处理的回调函数
    // （获取由Worker传递回来的obj，从obj中获取子结果，并进行最终结果的累加/合并）
    private void resultCallBack(Object o) {
        Task<R> task = (Task<R>) o;
        String taskName = "Worker:" + task.getWorkerId() + "-" + "Task:" + task.getId();
//        Print.tco(taskName + ":" + task.getResult());
        R subRes = task.getSubRes();
        resultMap.put(taskName, subRes);
        sumMerged.getAndAdd((Integer) subRes);  // 执行累加的归并（合并子任务的结果）
    }

    // 启动所有的子任务 - master负责轮流分配任务
    public void executeThread() {
        for (; ; ) {
            // 从任务队列中获取任务，然后Worker节点轮询,  轮流分配任务
            for (Map.Entry<String, Worker<T, R>> entry : workersMap.entrySet()) {
                T task = null;
                try {
                    task = this.taskQueue.take(); // 轮询提取一个任务，指派它去执行（累加）
                    Worker worker = entry.getValue(); // 任务执行者
                    // 回收task，重新放入任务池 & Master进行结果合并（回调）
                    // 参数2：方法引用“::”, 无返回值，只执行动作即可
                    worker.submitTaskToMaster(task, this::resultCallBack);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 获取最终的结果
    public void printResult() {
        Print.tco("----------sum is :" + sumMerged.get());
        for (Map.Entry<String, R> entry : resultMap.entrySet()) {
            String taskName = entry.getKey();
            Print.fo(taskName + "--> curSum = " + entry.getValue());
        }
    }

}
