package com.jav.thread.executor;


import com.jav.common.log.LogDog;
import com.jav.thread.executor.joggle.ILoopTaskExecutor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 循环任务执行者
 *
 * @author yyz
 */
public class LoopTaskExecutor implements ILoopTaskExecutor {

    private final Lock mLock = new ReentrantLock();
    private final Condition mCondition = mLock.newCondition();

    private final TaskContainer mContainer;

    /**
     * 已销毁
     */
    private final static int DESTROY = -1;
    /**
     * 未激活
     */
    private final static int INACTIVATED = 0;
    /**
     * 激活
     */
    private final static int ACTIVATION = 1;

    /*** 任务*/
    protected LoopTask mExecutorTask;

    private final ExecutorEngine mEngine;

    /*** 线程准备运行状态*/
    private volatile AtomicBoolean mIsStart = new AtomicBoolean(false);

    /*** 线程准备停止状态*/
    private volatile AtomicBoolean mIsStop = new AtomicBoolean(false);

    /*** 线程激活状态*/
    private volatile AtomicInteger mAliveState = new AtomicInteger(0);

    /*** 线程是否循环执行*/
    private volatile AtomicBoolean mIsLoop = new AtomicBoolean(true);

    /*** 线程是否暂停执行*/
    private volatile AtomicBoolean mIsPause = new AtomicBoolean(false);

    /*** 线程空闲状态标志位*/
    private volatile AtomicBoolean mIsIdle = new AtomicBoolean(false);

    /*** 是否复用线程*/
    private volatile AtomicBoolean mIsMultiplex = new AtomicBoolean(false);

    /**
     * 创建任务
     *
     * @param task      任务
     * @param container 任务容器
     */
    protected LoopTaskExecutor(LoopTask task, TaskContainer container) {
        if (task == null || container == null) {
            throw new NullPointerException("task or container is null");
        }
        this.mExecutorTask = task;
        this.mContainer = container;
        mEngine = new ExecutorEngine();
    }


    private class ExecutorEngine implements Runnable {

        @Override
        public void run() {
            mIsStart.set(false);
            // 设置激活状态
            mAliveState.set(ACTIVATION);
            // notify wait thread
            notifyWaitThread();

            do {
                // 执行初始化事件
                mExecutorTask.onInitTask();
                do {
                    // 是否暂停执行
                    if (mIsPause.get()) {
                        waitTask();
                    }
                    // 执行任务事件
                    mExecutorTask.onRunLoopTask();

                } while (mIsLoop.get());

                // 执行销毁事件
                mExecutorTask.onDestroyTask();

                if (mIsMultiplex.get()) {
                    // 设置线程空闲状态
                    mIsIdle.set(true);
                    mExecutorTask = null;
                    // 线程挂起 等待切换任务或者停止
                    waitChangeTask();
                    // 设置任务为循环状态
                    mIsLoop.set(true);
                }

            } while (mIsMultiplex.get());

            mIsStop.set(false);
            // 设置销毁状态
            mAliveState.set(DESTROY);
            // notify wait thread
            notifyWaitThread();
        }
    }

    protected ExecutorEngine getEngine() {
        return mEngine;
    }

    // -------------------End run -------------------------------

    // -------------------start status --------------------------

    @Override
    public boolean isAliveState() {
        return mAliveState.get() == ACTIVATION;
    }

    @Override
    public boolean isLoopState() {
        return mIsLoop.get();
    }

    @Override
    public boolean isPauseState() {
        return mIsPause.get();
    }

    @Override
    public boolean isIdleState() {
        return mIsIdle.get();
    }

    @Override
    public boolean isStartState() {
        return mIsStart.get();
    }

    @Override
    public boolean isStopState() {
        return mIsStop.get();
    }

    @Override
    public boolean isMultiplexState() {
        return mIsMultiplex.get();
    }

    @Override
    public void setMultiplexTask(boolean multiplex) {
        if (!isAliveState()) {
            return;
        }
        mIsMultiplex.set(multiplex);
    }

    // -------------------End status ----------------------------

    @Override
    public void pauseTask() {
        while (!mIsPause.get() && isAliveState() && !isStopState()) {
            mIsPause.set(true);
        }
    }

    @Override
    public void resumeTask() {
        wakeUpFromPause();
    }


    // -------------------End pauseTask resumeTask-----------------


    @Override
    public void startTask() {
        if (mIsStart.get() || mAliveState.get() == ACTIVATION || mAliveState.get() == DESTROY) {
            LogDog.w("## The thread has been started or has been destroyed !");
            return;
        }
        mIsStart.set(true);
        mContainer.getThread().start();
    }

    @Override
    public void blockStartTask() {
        startTask();
        // 循环等待状态被改变
        if (mContainer.getThread() != Thread.currentThread()) {
            if (mAliveState.get() == INACTIVATED && mIsStart.get()) {
                mLock.lock();
                try {
                    mCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mLock.unlock();
                }
            }
        }
    }


    @Override
    public void stopTask() {
        boolean isInvalid = mAliveState.get() == DESTROY || mIsStop.get();
        boolean isNotStart = !mIsStart.get() && mAliveState.get() == INACTIVATED;
        if (isInvalid || isNotStart) {
            LogDog.w("## The thread has not been activated or has been stopped !");
            return;
        }
        mIsStop.set(true);
        mIsLoop.set(false);
        if (isPauseState()) {
            wakeUpFromPause();
        } else {
            notifyWaitThread();
        }
    }

    @Override
    public void blockStopTask() {
        stopTask();
        // 循环等待状态被改变
        if (mContainer.getThread() != Thread.currentThread()) {
            if (isAliveState() && mIsStop.get()) {
                mLock.lock();
                try {
                    mCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mLock.unlock();
                }
            }
        }
    }

    // ------------------End startTask stopTask ----------------

    private void notifyWaitThread() {
        mLock.lock();
        try {
            mCondition.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 暂停线程
     */
    protected void waitTask() {
        waitTask(0);
    }


    protected void waitChangeTask() {
        if (isAliveState()) {
            mLock.lock();
            try {
                mIsPause.set(true);
                while (mIsMultiplex.get() && mIsIdle.get()) {
                    mCondition.await();
                }
                mIsPause.set(false);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mLock.unlock();
            }
        }
    }

    @Override
    public synchronized boolean changeTask(LoopTask task) {
        if (task == null || !isIdleState() || !isMultiplexState() || !isAliveState() || isStopState()) {
            return false;
        }
        this.mExecutorTask = task;
        mContainer.getThread().setName(task.getClass().getName());
        // 设置线程空闲状态位
        mIsIdle.set(false);
        notifyWaitThread();
        return true;
    }

    /**
     * 暂停线程
     *
     * @param time 等待的时间
     */
    @Override
    public void waitTask(long time) {
        if (isAliveState() && mIsLoop.get()) {
            mLock.lock();
            try {
                pauseTask();
                if (time == 0) {
                    mCondition.await();
                } else {
                    do {
                        boolean ret = mCondition.await(time, TimeUnit.MILLISECONDS);
                        // 没有达到通知时间则返回true
                        if (!ret) {
                            break;
                        }
                    } while (true);
                }
                while (mIsPause.get()) {
                    mIsPause.set(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mLock.unlock();
            }
        }
    }

    /**
     * 唤醒线程
     */
    protected void wakeUpFromPause() {
        if (mIsPause.get()) {
            mLock.lock();
            try {
                while (mIsPause.get()) {
                    mIsPause.set(false);
                }
                mCondition.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mLock.unlock();
            }
        }
    }

    @Override
    public void sleepTask(long time) {
        if (isAliveState()) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroyTask() {
        mIsMultiplex.set(false);
        stopTask();
    }

    // -------------------End waitTask wakeUpTask sleepTask-----------------
}
