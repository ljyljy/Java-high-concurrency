package com.ljy.designmodel.masterworker;

import com.ljy.util.Print;
import com.ljy.util.ThreadUtil;

import java.util.concurrent.TimeUnit;

public class MasterWorkerTest {
    //简单任务
    static class SimpleTask extends Task<Integer> {
        @Override
        protected Integer doExecute() {
            Print.tcfo("task " + getId() + " is done ");
            return getId(); // 每个子任务，累加的是自身id值，作为rst
        }
    }

    public static void main(String[] args) {
        //创建Master ，包含四个worker，并启动master的执行线程
        Master<SimpleTask, Integer> master = new Master<>(4);

        //定期向master提交任务（每2s）
        ThreadUtil.scheduleAtFixedRate(() -> master.addTask(new SimpleTask()),
                2, TimeUnit.SECONDS);

        //定期从master提取结果（每5s）
        ThreadUtil.scheduleAtFixedRate(() -> master.printResult(),
                5, TimeUnit.SECONDS);
    }

}
