package task.executor;


/**
 * 循环任务
 * Created by prolog on 2018/2/16.
 *
 * @author yyz
 */
public class BaseLoopTask {

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
     * 任务进入懒关闭状态，处理完该回调即将调用onDestroyTask
     */
    protected void onIdleStop() {
        //Do something
    }

    /**
     * 任务销毁
     */
    protected void onDestroyTask() {
        //Do something
    }
}
