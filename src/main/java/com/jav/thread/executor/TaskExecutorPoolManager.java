package com.jav.thread.executor;


import com.jav.common.log.LogDog;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程池
 *
 * @author YDZ
 */
public class TaskExecutorPoolManager {

    /**
     * 缓存线程栈
     */
    private final ConcurrentLinkedQueue<TaskContainer> mContainerCache = new ConcurrentLinkedQueue<>();

    public TaskExecutorPoolManager() {
    }

    private static class Inner {
        private static final TaskExecutorPoolManager sPool = new TaskExecutorPoolManager();
    }

    public static TaskExecutorPoolManager getInstance() {
        return Inner.sPool;
    }

    private TaskContainer getThreadContainer() {
        if (!mContainerCache.isEmpty()) {
            synchronized (mContainerCache) {
                for (TaskContainer container : mContainerCache) {
                    LoopTaskExecutor executor = container.getTaskExecutor();
                    boolean isIdleState = executor.isIdleState();
                    if (isIdleState) {
                        return container;
                    }
                }
            }
        }
        return null;
    }


    public void runTask(LoopTask loopTask) {
        if (loopTask == null) {
            return;
        }
        TaskContainer container = getThreadContainer();
        if (container != null) {
            LoopTaskExecutor executor = container.getTaskExecutor();
            executor.changeTask(loopTask);
        } else {
            container = new TaskContainer(loopTask);
            LoopTaskExecutor executor = container.getTaskExecutor();
            executor.setMultiplexTask(true);
            mContainerCache.add(container);
            executor.startTask();
        }
    }


    public void closeTask(LoopTask loopTask) {
        for (TaskContainer container : mContainerCache) {
            LoopTaskExecutor executor = container.getTaskExecutor();
            if (executor.mExecutorTask == loopTask) {
                executor.blockStopTask();
            }
        }
    }


    public void multiplexThread(TaskContainer container) {
        if (container == null) {
            LogDog.e("## container  is null !!! ");
            return;
        }
        LoopTaskExecutor executor = container.getTaskExecutor();
        boolean state = executor.isMultiplexState() && executor.isIdleState();
        if (state) {
            mContainerCache.add(container);
        }
    }


    public void destroyAll() {
        for (TaskContainer container : mContainerCache) {
            LoopTaskExecutor executor = container.getTaskExecutor();
            executor.destroyTask();
        }
        mContainerCache.clear();
    }
}