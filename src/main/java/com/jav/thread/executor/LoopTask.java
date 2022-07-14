package com.jav.thread.executor;


/**
 * 循环任务事务
 * Created by prolog on 2018/2/16.
 *
 * @author yyz
 */
public class LoopTask {

    /**
     * 初始化任务
     */
    protected void onInitTask() {
        //Do something
    }

    /**
     * 循环执行
     */
    protected void onRunLoopTask() {
        //Do something
    }

    /**
     * 任务销毁
     */
    protected void onDestroyTask() {
        //Do something
    }

    /**
     * 任务开始进入空闲状态
     */
    protected void onInIdleTask() {
        //Do something
    }

    /**
     * 任务开始退出空闲状态
     */
    protected void onOutIdleTask() {
        //Do something
    }
}
