package task.executor.joggle;

/**
 * 消费任务执行者接口
 * Created by No.9 on 2018/2/18.
 *
 * @author yyz
 */

public interface IConsumerTaskExecutor<D>{


    /**
     * 设置没有缓存区没有数据处理则进入休眠状态
     *
     * @param state true 则进入休眠状态
     */
    void setIdleStateSleep(boolean state);



    /**
     * 开启异步处理数据模式,
     * 开启后 onCreateData ,onProcess 分别不同线程来执行
     */
    void startAsyncProcessData();


    /**
     * 关闭异步处理数据模式
     */
    void stopAsyncProcessData();

    /**
     * 当前是否异步处理数据
     *
     * @return
     */
    ILoopTaskExecutor getAsyncTaskExecutor();


}
