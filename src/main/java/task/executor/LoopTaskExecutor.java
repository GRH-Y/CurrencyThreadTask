package task.executor;


import task.executor.joggle.IAttribute;
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

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    private TaskContainer container;

    /*** 任务*/
    protected BaseLoopTask executorTask;
    /*** 执行接口*/
    private LoopEngine engine;

    /*** 线程状态*/
    private volatile boolean isAlive = false;

    /*** 线程准备运行状态*/
    private volatile boolean isStart = false;

    /*** 线程是否循环执行*/
    private volatile boolean isLoop = false;

    /*** 线程是否暂停执行*/
    private volatile boolean isPause = false;

    /*** 线程空闲状态标志位*/
    private volatile boolean isIdle = true;

    /*** 懒停止（提交完缓存区数据后再关闭）*/
    private volatile boolean idleStop = false;

    /*** 是否复用线程*/
    private volatile boolean isMultiplex = false;

    /**
     * 创建任务
     *
     * @param container 任务容器
     */
    LoopTaskExecutor(TaskContainer container) {
        if (container == null) {
            throw new NullPointerException(" container is null");
        }
        this.container = container;
        this.executorTask = container.getTask();
        engine = new LoopEngine();
    }

    protected Runnable getRunnable() {
        return engine;
    }

    @Override
    public <T> T getAttribute() {
        return null;
    }

    @Override
    public void setAttribute(IAttribute attribute) {
    }

    private class LoopEngine implements Runnable {

        @Override
        public void run() {
            //设置线程激活状态
            isAlive = true;
            // 设置线程非空闲状态
            isIdle = false;
            //notify wait thread
            notifyWaitThread();

            do {
                //执行初始化事件
                executorTask.onInitTask();
                do {
                    // 是否暂停执行
                    if (isPause) {
                        waitTask();
                    }
                    // 执行任务事件
                    executorTask.onRunLoopTask();

                } while (isLoop);

                // 执行任务懒关闭事件
                if (idleStop) {
                    executorTask.onIdleStop();
                }
                //执行销毁事件
                executorTask.onDestroyTask();

                if (isMultiplex) {
                    // 设置线程空闲状态
                    isIdle = true;
                    //notify wait thread
                    notifyWaitThread();
                    // 线程挂起 等待切换任务或者停止
                    waitChangeTask();
                    // 设置线程非空闲状态
                    isIdle = false;
                    if (isMultiplex) {
                        // 设置任务为循环状态
                        isLoop = true;
                    }
                }

            } while (isMultiplex);

            isStart = false;
            isAlive = false;
            //notify wait thread
            notifyWaitThread();

            executorTask = null;
            container = null;
            engine = null;
        }
    }

    private void notifyWaitThread() {
        lock.lock();
        try {
            condition.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    // -------------------End run ---------------------

    @Override
    public boolean getAliveState() {
        return isAlive;
    }

    // -------------------End get ThreadState---------------------

    @Override
    public boolean getLoopState() {
        return isLoop;
    }

    @Override
    public void setLoopState(boolean state) {
        isLoop = state;
    }

    // -------------------End get set LoopState setLoopInit ---------------------

    /**
     * 获取线程暂停状态
     *
     * @return true 为线程是暂停状态
     */
    @Override
    public boolean getPauseState() {
        return isPause;
    }

    @Override
    public boolean getIdleStopState() {
        return idleStop;
    }

    // -------------------End setPauseState getPauseState-----------------

    @Override
    public void pauseTask() {
        isPause = true;
    }

    @Override
    public void resumeTask() {
        wakeUpTask();
    }

    // -------------------End pauseTask resumeTask-----------------

    @Override
    public void startTask() {
        startTask(executorTask.getClass().getName());
    }

    @Override
    public void startTask(String threadName) {
        if (!isAlive && !isStart) {
            isStart = true;
            isLoop = true;
            try {
                container.getThread().setName(threadName);
                container.getThread().start();
            } catch (Throwable e) {
                Thread thread = container.getNewThread();
                thread.setName(threadName);
                thread.start();
            }
        }
    }

    @Override
    public void blockStartTask() {
        blockStartTask(executorTask.getClass().getName());
    }

    @Override
    public void blockStartTask(String threadName) {
        startTask(threadName);
        //循环等待状态被改变
        if (!(container.getThread() == Thread.currentThread())) {
            if (!isAlive && isStart) {
                lock.lock();
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    @Override
    public void idleStopTask() {
        idleStop = true;
        stopTask();
    }


    @Override
    public void stopTask() {
        isLoop = false;
        wakeUpTask();
    }

    @Override
    public void blockStopTask() {
        stopTask();
        //循环等待状态被改变
        if (!(container.getThread() == Thread.currentThread())) {
            if (isAlive && isStart) {
                lock.lock();
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    // ------------------End startTask stopTask ----------------

    /**
     * 暂停线程
     */
    protected void waitTask() {
        waitTask(0);
    }

    protected void waitChangeTask() {
        if (isAlive) {
            lock.lock();
            isPause = true;
            try {
                while (isMultiplex && isIdle) {
                    condition.await();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isPause = false;
                lock.unlock();
            }
        }
    }

    /**
     * 暂停线程
     *
     * @param time 等待的时间
     */
    @Override
    public void waitTask(long time) {
        if (isAlive && isLoop) {
            lock.lock();
            try {
                isPause = true;
                if (time == 0) {
                    condition.await();
                } else {
                    condition.await(time, TimeUnit.MILLISECONDS);
                }
                isPause = false;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 唤醒线程
     */
    protected void wakeUpTask() {
        if (isPause) {
            lock.lock();
            try {
                condition.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void sleepTask(long time) {
        if (isLoop) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    // -------------------End waitTask wakeUpTask sleepTask-----------------


    @Override
    public synchronized boolean changeTask(BaseLoopTask task) {
        if (!isIdleState()) {
            return false;
        }
        this.executorTask = task;
        //设置线程空闲状态位
        this.isIdle = false;
        wakeUpTask();
        return true;
    }


    @Override
    public boolean isIdleState() {
        return isIdle && isAlive && isStart && isMultiplex;
    }

    @Override
    public boolean isStartState() {
        return isStart;
    }

    @Override
    public void destroyTask() {
        isMultiplex = false;
        stopTask();
    }

    @Override
    public boolean getMultiplexState() {
        return isMultiplex;
    }

    @Override
    public void setMultiplexTask(boolean multiplex) {
        isMultiplex = multiplex;
    }
}
