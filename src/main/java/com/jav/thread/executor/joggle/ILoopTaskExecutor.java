package com.jav.thread.executor.joggle;

import com.jav.thread.executor.LoopTask;

/**
 * 控制循环任务接口
 * Created by dell on 2/8/2018.
 *
 * @author yyz
 */
public interface ILoopTaskExecutor {

    /**
     * 任务是否是循环状态
     *
     * @return true 为循环状态
     */
    boolean isLoopState();


    /**
     * 获取任务执行的状态
     *
     * @return true 任务还在运行
     **/
    boolean isAliveState();


    /**
     * 任务是否是暂停状态
     *
     * @return
     */
    boolean isPauseState();

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
     * 开始执行任务
     */
    void startTask();

    /**
     * 阻塞式开始执行任务
     */
    void blockStartTask();

    /**
     * 停止运行任务
     */
    void stopTask();

    /**
     * 阻塞式停止执行
     */
    void blockStopTask();


    /**
     * 线程准备运行状态
     *
     * @return true 为开始运行
     */
    boolean isStartState();


    /**
     * 线程准备结束
     * @return
     */
    boolean isStopState();


    /**
     * 切换任务
     *
     * @param runnable 任务
     * @return true 切换任务成功
     */
    boolean changeTask(LoopTask runnable);


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
    boolean isMultiplexState();

    /**
     * 设置线程是否是复用线程
     *
     * @param multiplex true 为复用线程
     */
    void setMultiplexTask(boolean multiplex);


}
