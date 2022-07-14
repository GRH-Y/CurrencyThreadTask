package com.jav.thread.executor;


import com.jav.common.util.StringEnvoy;

/**
 * 线程实体
 *
 * @author yyz
 */
public class TaskContainer {

    /**
     * 线程体
     */
    private final Thread mThread;
    /**
     * 执行体
     */
    private final LoopTaskExecutor mExecutor;


    /**
     * 创建循环任务任务
     *
     * @param task 循环任务
     */
    public TaskContainer(LoopTask task) {
        this(task, task.getClass().getName());
    }

    public TaskContainer(LoopTask task, String threadName) {
        if (task == null || StringEnvoy.isEmpty(threadName)) {
            throw new NullPointerException("task or threadName is null");
        }
        mExecutor = new LoopTaskExecutor(task, this);
        mThread = new Thread(mExecutor.getEngine(), threadName);
    }

    public void setThreadPriority(int newPriority) {
        mThread.setPriority(newPriority);
    }

    protected Thread getThread() {
        return mThread;
    }


    public LoopTaskExecutor getTaskExecutor() {
        return mExecutor;
    }

}
