package task.executorpool.interfaces;


import task.executor.BaseConsumerTask;
import task.executor.BaseLoopTask;
import task.executor.interfaces.ITaskContainer;

/**
 * 可复用的线程接口
 *
 * @author yyz
 */
public interface IThreadPoolManager {
    /**
     * 添加任务,线程池会自动执行
     *
     * @param runnable
     */
    void addTask(Runnable runnable);

    /**
     * 添加任务,线程池会自动执行
     *
     * @param loopTask
     */
    void addTask(BaseLoopTask loopTask);

    /**
     * 添加任务,线程池会自动执行
     *
     * @param consumerTask
     */
    void addTask(BaseConsumerTask consumerTask);


    /**
     * 回收线程（线程不是销毁状态才可以用）
     *
     * @param thread 线程
     */
    void recycleThread(ITaskContainer thread);

    /**
     * 切换任务
     *
     * @param runnable 任务
     * @return true 切换任务成功
     */
    boolean changeTask(ITaskContainer thread, BaseLoopTask runnable);

    /**
     * 销毁线程
     */
    void destroy(ITaskContainer thread);

    /**
     * 线程是否空闲状态
     *
     * @return true 空闲
     */
    boolean isIdleState();

    /**
     * 释放资源
     */
    void recycle();
}
