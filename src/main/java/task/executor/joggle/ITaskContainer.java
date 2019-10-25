package task.executor.joggle;


import task.executor.BaseLoopTask;

/**
 * 线程容器
 * 存储了线程和任务
 *
 * @author yyz
 */
public interface ITaskContainer {

//    /**
//     * 获取线程体
//     *
//     * @return 获取线程实体
//     */
//    Thread getThread();
//
//    /**
//     * 获取新的线程体（该方法不要滥用）
//     *
//     * @return 返回新的线程实体
//     */
//    Thread getNewThread();

    /**
     * 获取任务执行器（可以控制任务的状态）
     *
     * @param <T> 执行器
     * @return 返回任务执行器
     */
    <T extends ILoopTaskExecutor> T getTaskExecutor();

    /**
     * 获取任务
     *
     * @return
     */
    <T extends BaseLoopTask> T getTask();

}
