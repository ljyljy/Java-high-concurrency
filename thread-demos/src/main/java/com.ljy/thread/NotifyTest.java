package com.ljy.thread;

import com.ljy.util.ThreadUtil;
import org.junit.Test;

public class NotifyTest {

    Object lock = new Object();


    @Test
    public void testNotify() {
        synchronized (lock) {
            lock.notify();
        }

        ThreadUtil.sleepMilliSeconds(1000000);
    }
}
