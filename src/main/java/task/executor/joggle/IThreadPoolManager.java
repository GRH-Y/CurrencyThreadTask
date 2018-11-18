package task.executor.joggle;


import task.executor.BaseConsumerTask;
import task.executor.BaseLoopTask;

/**
 * 可复用的线程接口
 *
 * @author yyz
 */
public interface IThreadPoolManager {

    ITaskContainer createJThread(BaseLoopTask loopTask);

    ITaskContainer createJThread(BaseConsumerTask consumerTask);

    /**
     * 添加任务,线程池会自动执行
     *
     * @param loopTask
     */
    ITaskContainer runTask(BaseLoopTask loopTask, IAttribute attribute);

    /**
     * 添加任务,线程池会自动执行
     *
     * @param consumerTask
     */
    ITaskContainer runTask(BaseConsumerTask consumerTask, IAttribute attribute);

    /**
     * 移除任务
     *
     * @param loopTask
     */
    void removeTask(BaseLoopTask loopTask);

    /**
     * 移除任务
     *
     * @param loopTask
     */
    void removeTask(BaseConsumerTask loopTask);


    /**
     * 回收线程（线程不是销毁状态才可以用）
     *
     * @param container 线程
     */
    void recycleThread(ITaskContainer container);

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
    void destroy(ITaskContainer container);

    /**
     * 线程是否空闲状态
     *
     * @return true 空闲
     */
    boolean isIdleState(ITaskContainer container);

    /**
     * 释放资源
     */
    void destroyAll();
}
