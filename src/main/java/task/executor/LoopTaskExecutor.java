package task.executor;


import task.executor.interfaces.ILoopTaskExecutor;
import task.executor.interfaces.ITaskContainer;

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

    private Lock lock = new ReentrantLock(true);
    private Condition condition = lock.newCondition();

    private ITaskContainer container;

    /*** 任务*/
    protected BaseLoopTask loopTask;
    /*** 执行接口*/
    private Engine engine;

    /*** 线程状态*/
    private volatile boolean isAlive = false;

    /*** 线程准备运行状态*/
    private volatile boolean isStart = false;

    /*** 是否循环执行初始化状态*/
    private volatile boolean isLoopInit = false;

    /*** 线程是否循环执行*/
    private volatile boolean isLoop = true;

    /*** 线程是否暂停执行*/
    private volatile boolean isPause = false;

    /*** 线程空闲状态标志位*/
    private volatile boolean isIdle = false;

    /*** 懒停止（提交完缓存区数据后再关闭）*/
    private volatile boolean idleStop = false;

    /*** 是否复用线程*/
    private volatile boolean isMultiplex = false;

    /**
     * 创建任务
     *
     * @param container 任务容器
     */
    protected LoopTaskExecutor(TaskContainer container) {
        if (container == null) {
            throw new NullPointerException(" container is null");
        }
        this.container = container;
        this.loopTask = container.getTask();
        engine = new Engine();
    }

    @Override
    public Runnable getRunnable() {
        return engine;
    }


    private class Engine implements Runnable {

        @Override
        public void run() {
            //设置线程激活状态
            isAlive = true;
            // 设置线程非空闲状态
            isIdle = false;

            do {
                //执行初始化事件
                loopTask.onInitTask();
                while (getLoopState()) {
                    // 是否暂停执行
                    if (getPauseState() && getLoopState()) {
                        waitTask();
                    }
                    if (getLoopState()) {
                        // 执行任务事件
                        loopTask.onRunLoopTask();
                    }
                    if (isLoopInit && getLoopState()) {
                        loopTask.onInitTask();
                    }
                }
                // 执行任务懒关闭事件
                if (getIdleStopState()) {
                    loopTask.onIdleStop();
                }
                //执行销毁事件
                loopTask.onDestroyTask();

                if (getMultiplexState()) {
                    // 设置线程空闲状态
                    setIdleState(true);
                    // 线程挂起 等待切换任务或者停止
                    waitTask();
                    // 设置线程非空闲状态
                    setIdleState(false);
                    // 设置任务为循环状态
                    setLoopState(true);
                }

            } while (getMultiplexState());

            isStart = false;
            isAlive = false;
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

    /**
     * 每次循环onRunTask 执行onInitTask
     *
     * @param isLoop 默认是false，true则循环一次则调用onInitTask()
     */
    @Override
    public void setLoopInit(boolean isLoop) {
        isLoopInit = isLoop;
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
        startTask(loopTask.getClass().getName());
    }

    @Override
    public void startTask(String threadName) {
        if (!getAliveState() && !isStartState()) {
            setLoopState(true);
            try {
                container.getThread().setName(threadName);
                container.getThread().start();
            } catch (Throwable e) {
                Thread thread = container.getNewThread();
                thread.setName(threadName);
                thread.start();
            }
            isStart = true;
        }
    }

    @Override
    public void blockStartTask() {
        blockStartTask(loopTask.getClass().getName());
    }

    @Override
    public void blockStartTask(String threadName) {
        while (!getLoopState() && !getMultiplexState() && getAliveState()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        startTask(threadName);
        //循环等待状态被改变
        while (!getAliveState() && isStartState()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        setLoopState(false);
        resumeTask();
    }

    @Override
    public void blockStopTask() {
        stopTask();
        //循环等待状态被改变
        while (getAliveState() && !isStartState()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    /**
     * 暂停线程
     *
     * @param time 等待的时间
     */
    @Override
    public void waitTask(long time) {
        if (getAliveState()) {
            try {
                lock.lock();
                if (isLoop) {
                    isPause = true;
                    if (time == 0) {
                        condition.await();
                    } else {
                        condition.await(time, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                isPause = false;
            }
        }
    }

    /**
     * 唤醒线程
     */
    protected void wakeUpTask() {
        if (isPause) {
            lock.lock();
            isPause = false;
            condition.signal();
            lock.unlock();
        }
    }

    @Override
    public void sleepTask(long time) {
        if (getLoopState()) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // -------------------End waitTask wakeUpTask sleepTask-----------------


    @Override
    public final boolean changeTask(BaseLoopTask task) {
        if (isIdleState()) {
            this.loopTask = task;
        }
        return this.loopTask == task;
    }

    /**
     * 设置线程空闲状态位
     *
     * @param state
     */
    private void setIdleState(boolean state) {
        this.isIdle = state;
    }

    @Override
    public boolean isIdleState() {
        return isIdle;
    }

    @Override
    public boolean isStartState() {
        return isStart;
    }

    @Override
    public void destroyTask() {
        setMultiplexTask(false);
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
