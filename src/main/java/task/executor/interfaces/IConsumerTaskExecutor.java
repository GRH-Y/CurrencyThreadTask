package task.executor.interfaces;

/**
 * 消费任务执行者接口
 * Created by No.9 on 2018/2/18.
 *
 * @author yyz
 */

public interface IConsumerTaskExecutor<D> {


    /**
     * 设置没有缓存区没有数据处理则进入休眠状态
     *
     * @param state true 则进入休眠状态
     */
    void setIdleStateSleep(boolean state);


}
