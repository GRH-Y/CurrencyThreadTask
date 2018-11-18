package task.executor.joggle;

import task.executor.BaseLoopTask;

/**
 * 控制循环任务接口
 * Created by dell on 2/8/2018.
 *
 * @author yyz
 */
public interface ILoopTaskExecutor {


    <T> T getAttribute();

    /**
     * 设置扩展属性
     */
    void setAttribute(IAttribute attribute);

    /**
     * 设置任务是否循环执行
     *
     * @param state true 为循环模式
     */
    void setLoopState(boolean state);

    /**
     * 任务是否是循环状态
     *
     * @return true 为循环状态
     */
    boolean getLoopState();

    /**
     * 获取执行接口
     *
     * @return
     */
    Runnable getRunnable();

    /**
     * 获取任务执行的状态
     *
     * @return true 任务还在运行
     **/
    boolean getAliveState();

    /**
     * 每次循环会执行一次 onInitTask
     *
     * @param isLoop true 为每次循环会执行一次
     */
    void setLoopInit(boolean isLoop);

    /**
     * 任务是否是暂停状态
     *
     * @return
     */
    boolean getPauseState();

    /**
     * 获取当前任务是否是懒关闭状态
     *
     * @return true 为懒关闭状态
     */
    boolean getIdleStopState();

    /**
     * 暂停执行任务
     */
    void pauseTask();

    /**
     * 继续执行任务
     */
    void resumeTask();


    /**
     * 任务等待
     *
     * @param timeout
     */
    void waitTask(long timeout);

    /**
     * 任务线程睡眠
     *
     * @param time
     */
    void sleepTask(long time);

    /**
     * 开始执行任务
     */
    void startTask();

    /**
     * 开始执行任务
     *
     * @param threadName 线程名称，方便调试
     */
    void startTask(String threadName);

    /**
     * 阻塞式开始执行任务
     */
    void blockStartTask();

    /**
     * 阻塞式开始执行任务
     *
     * @param threadName 线程名称，方便调试
     */
    void blockStartTask(String threadName);

    /**
     * 停止运行任务
     */
    void stopTask();

    /**
     * 阻塞式停止执行
     */
    void blockStopTask();

    /**
     * 懒关闭方式停止任务
     */
    void idleStopTask();


    /**
     * 线程准备运行状态
     *
     * @return true 为开始运行
     */
    boolean isStartState();


    /**
     * 切换任务
     *
     * @param runnable 任务
     * @return true 切换任务成功
     */
    boolean changeTask(BaseLoopTask runnable);


    /**
     * 设置线程不可复用并停止线程
     */
    void destroyTask();

    /**
     * 线程是否空闲状态
     *
     * @return true 空闲
     */
    boolean isIdleState();

    /**
     * 获取当前线程是否是复用线程
     *
     * @return true 为复用线程
     */
    boolean getMultiplexState();

    /**
     * 设置线程是否是复用线程
     *
     * @param multiplex true 为复用线程
     */
    void setMultiplexTask(boolean multiplex);


}
