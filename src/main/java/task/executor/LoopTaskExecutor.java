package task.executor;


import task.executor.joggle.ILoopTaskExecutor;

import java.util.concurrent.TimeUnit;
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

    /*** 任务*/
    protected LoopTask mExecutorTask;

    private final ExecutorEngine mEngine;

    /*** 线程状态*/
    private volatile boolean mIsAlive = false;

    /*** 线程准备运行状态*/
    private volatile boolean mIsStart = false;

    /*** 线程是否循环执行*/
    private volatile boolean mIsLoop = false;

    /*** 线程是否暂停执行*/
    private volatile boolean mIsPause = false;

    /*** 线程空闲状态标志位*/
    private volatile boolean mIsIdle = true;

    /*** 是否复用线程*/
    private volatile boolean mIsMultiplex = false;

    /**
     * 创建任务
     *
     * @param task 任务
     * @param task 任务名字
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
            //设置线程激活状态
            mIsAlive = true;
            // 设置线程非空闲状态
            mIsIdle = false;
            //notify wait thread
            notifyWaitThread();

            do {
                //执行初始化事件
                mExecutorTask.onInitTask();
                do {
                    // 是否暂停执行
                    if (mIsPause) {
                        waitTask();
                    }
                    // 执行任务事件
                    mExecutorTask.onRunLoopTask();

                } while (mIsLoop);

                //执行销毁事件
                mExecutorTask.onDestroyTask();

                if (mIsMultiplex) {
                    // 设置线程空闲状态
                    mIsIdle = true;
                    mExecutorTask.onInIdleTask();
                    //notify wait thread
                    notifyWaitThread();
                    // 线程挂起 等待切换任务或者停止
                    waitChangeTask();
                    // 设置线程非空闲状态
                    mIsIdle = false;
                    mExecutorTask.onOutIdleTask();
                    if (mIsMultiplex) {
                        // 设置任务为循环状态
                        mIsLoop = true;
                    }
                }

            } while (mIsMultiplex);

            mIsStart = false;
            mIsAlive = false;
            //notify wait thread
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
        return mIsAlive;
    }

    @Override
    public boolean isLoopState() {
        return mIsLoop;
    }

    /**
     * 获取线程暂停状态
     *
     * @return true 为线程是暂停状态
     */
    @Override
    public boolean isPauseState() {
        return mIsPause;
    }

    @Override
    public boolean isIdleState() {
        return mIsIdle && mIsAlive && mIsStart && mIsMultiplex;
    }

    @Override
    public boolean isStartState() {
        return mIsStart;
    }

    @Override
    public boolean isMultiplexState() {
        return mIsMultiplex;
    }

    @Override
    public void setMultiplexTask(boolean multiplex) {
        mIsMultiplex = multiplex;
    }

    // -------------------End status ----------------------------

    @Override
    public void pauseTask() {
        mIsPause = true;
    }

    @Override
    public void resumeTask() {
        wakeUpTask();
    }


    // -------------------End pauseTask resumeTask-----------------


    @Override
    public void startTask() {
        if (!mIsAlive && !mIsStart) {
            mIsStart = true;
            mIsLoop = true;
            mContainer.getThread().start();
        }
    }

    @Override
    public void blockStartTask() {
        startTask();
        //循环等待状态被改变
        if (!(mContainer.getThread() == Thread.currentThread())) {
            if (!mIsAlive && mIsStart) {
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
        mIsLoop = false;
        wakeUpTask();
    }

    @Override
    public void blockStopTask() {
        stopTask();
        //循环等待状态被改变
        if (!(mContainer.getThread() == Thread.currentThread())) {
            if (mIsAlive && mIsStart) {
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
        if (mIsAlive) {
            mLock.lock();
            mIsPause = true;
            try {
                while (mIsMultiplex && mIsIdle) {
                    mCondition.await();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mIsPause = false;
                mLock.unlock();
            }
        }
    }

    @Override
    public synchronized boolean changeTask(LoopTask task) {
        if (task == null || !isIdleState()) {
            return false;
        }
        this.mExecutorTask = task;
        mContainer.getThread().setName(task.getClass().getName());
        //设置线程空闲状态位
        this.mIsIdle = false;
        wakeUpTask();
        return true;
    }

    /**
     * 暂停线程
     *
     * @param time 等待的时间
     */
    @Override
    public void waitTask(long time) {
        if (mIsAlive && mIsLoop) {
            mLock.lock();
            try {
                mIsPause = true;
                if (time == 0) {
                    mCondition.await();
                } else {
                    do {
                        boolean ret = mCondition.await(time, TimeUnit.MILLISECONDS);
                        //没有达到通知时间则返回true
                        if (!ret) {
                            break;
                        }
                    } while (true);
                }
                mIsPause = false;
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
    protected void wakeUpTask() {
        if (mIsPause) {
            mLock.lock();
            try {
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
        if (mIsLoop) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroyTask() {
        mIsMultiplex = false;
        stopTask();
    }

    // -------------------End waitTask wakeUpTask sleepTask-----------------
}
