package com.jav.thread.executor;


import com.jav.thread.executor.joggle.IAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * MultiTask 多线程并发处理任务类
 *
 * @author yyz
 * @date 2017/7/20.
 * Created by prolog on 2017/7/20.
 */

public abstract class MultiTask<T> {
    private List<StackTraceElement> mLockList;
    private List<Task> mTaskList;
    private int mToken = 0;
    /***生产标记*/
    private long mProductionMark = 0;
    /***消费标记*/
    private long mConsumerMark = 0;

    public MultiTask() {
        int count = Runtime.getRuntime().availableProcessors();
        init(count);
    }

    /**
     * 多线程并发处理任务
     *
     * @param count 开启线程的数量
     */
    public MultiTask(int count) {
        init(count);
    }

    private void init(int count) {
        mTaskList = new ArrayList<>(count);
        mLockList = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            Task task = new Task(index);
            mTaskList.add(task);
            task.getExecutor().startTask();
        }
    }

    /**
     * 设置线数据程缓冲区大小
     *
     * @param count 缓冲区大小
     */
    public void setCacheMaxCount(int count) {
        for (Task task : mTaskList) {
            task.getAttribute().setCacheMaxCount(count);
        }
    }

    /**
     * 存储要处理的数据
     *
     * @param data 数据
     */
    public void pushData(T data) {
        Task task = mTaskList.get(mToken++);
        TaskEntity entity = new TaskEntity(mProductionMark++, data);
        task.getAttribute().pushToCache(entity);
        task.getExecutor().resumeTask();
        if (mToken >= mTaskList.size()) {
            mToken = 0;
        }
    }

    /**
     * 获取锁
     *
     * @param task 当前任务线程
     *             //     * @param mark 当前任务的mark
     */
    protected synchronized void lock(Task task) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = elements[elements.length - 1];
        boolean isHas = false;
        for (StackTraceElement element : mLockList) {
            if (currentElement.getClassName().equals(element.getClassName())
                    && currentElement.getMethodName().equals(element.getMethodName())
                    && currentElement.getLineNumber() == element.getLineNumber()) {
                isHas = true;
                break;
            }
        }
        if (isHas && mTaskList.size() > 1) {
            task.getExecutor().waitTask(0);
        } else {
            mLockList.add(elements[elements.length - 1]);
        }
    }

    /**
     * 释放锁 通知其它线程可以继续运行
     */
    protected synchronized void unLock() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = elements[elements.length - 1];
        int index = 0;
        for (StackTraceElement element : mLockList) {
            if (currentElement.getClassName().equals(element.getClassName())
                    && currentElement.getMethodName().equals(element.getMethodName())
                    && currentElement.getLineNumber() == element.getLineNumber()) {
                break;
            }
            index++;
        }
        mLockList.remove(index);
        for (Task tmp : mTaskList) {
            tmp.getExecutor().resumeTask();
        }
    }

    protected synchronized void nextTask() {
        mConsumerMark++;
    }

    protected void onInitTask() {
    }

    protected void onReleaseTask() {
    }

    /**
     * 处理数据
     *
     * @param task 当前任务线程
     * @param data 要处理的数据
     * @param mark 当前任务的mark
     */
    protected abstract void onExecTask(Task task, T data, long mark);

    /**
     * 任务线程
     */
    protected class Task extends LoopTask {
        protected final int token;
        private final LoopTaskExecutor executor;
        private final IAttribute<TaskEntity> attribute;

        Task(int token) {
            TaskContainer container = new TaskContainer(this, "MultiTask.Task." + token);
            executor = container.getTaskExecutor();
            attribute = new ConsumerQueueAttribute<>();
            this.token = token;
        }

        public LoopTaskExecutor getExecutor() {
            return executor;
        }

        public int getToken() {
            return token;
        }

        public IAttribute<TaskEntity> getAttribute() {
            return attribute;
        }

        @Override
        protected void onInitTask() {
            MultiTask.this.onInitTask();
        }

        @Override
        protected void onRunLoopTask() {
            TaskEntity entity = attribute.popCacheData();
            if (entity != null) {
                onExecTask(this, entity.mData, entity.mMark);
            } else {
                executor.waitTask();
            }
        }


        @Override
        protected void onDestroyTask() {
            MultiTask.this.onReleaseTask();
        }

    }

    /**
     * 任务Entity
     */
    private class TaskEntity {
        long mMark;
        T mData;

        TaskEntity(long mark, T data) {
            this.mMark = mark;
            this.mData = data;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mTaskList != null) {
            for (Task task : mTaskList) {
                task.getExecutor().stopTask();
            }
            mTaskList.clear();
            mTaskList = null;
        }
    }
}

