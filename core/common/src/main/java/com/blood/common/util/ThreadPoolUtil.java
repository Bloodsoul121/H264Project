package com.blood.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtil {

    private final ExecutorService mExecutorService;

    private static volatile ThreadPoolUtil sThreadPoolUtil;

    public static ThreadPoolUtil getInstance() {
        if (sThreadPoolUtil == null) {
            synchronized (ThreadPoolUtil.class) {
                if (sThreadPoolUtil == null) {
                    sThreadPoolUtil = new ThreadPoolUtil();
                }
            }
        }
        return sThreadPoolUtil;
    }

    private ThreadPoolUtil() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    public void start(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

}
