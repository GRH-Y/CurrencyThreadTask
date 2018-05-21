package task.executor.interfaces;

/**
 * 控制循环任务接口
 * Created by dell on 2/8/2018.
 *
 * @author yyz
 */
public interface ILoopTaskExecutor extends ITaskExecutor {


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

}
