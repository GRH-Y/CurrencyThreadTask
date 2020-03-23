package task.executor;


import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;
import util.StringEnvoy;

/**
 * 线程实体
 *
 * @author yyz
 */
public class TaskContainer implements ITaskContainer {

    /**
     * 要执行的任务
     */
    private BaseLoopTask task;
    /**
     * 线程体
     */
    private Thread thread;
    /**
     * 执行体
     */
    private LoopTaskExecutor objectExecutor;


    /**
     * 创建循环任务任务
     *
     * @param task 循环任务
     */
    public TaskContainer(BaseLoopTask task) {
        this(task, task.getClass().getName());
    }

    public TaskContainer(BaseLoopTask task, String threadName) {
        if (task == null || StringEnvoy.isEmpty(threadName)) {
            throw new NullPointerException("task or threadName is null");
        }
        this.task = task;
        if (task instanceof BaseConsumerTask) {
            objectExecutor = new ConsumerTaskExecutor(this);
        } else {
            objectExecutor = new LoopTaskExecutor(this);
        }
        thread = new Thread(objectExecutor.getRunnable(), threadName);
    }

    protected Thread getNewThread() {
        thread = new Thread(objectExecutor.getRunnable(), objectExecutor.getClass().getName());
        return thread;
    }

    @Override
    public Thread getThread() {
        return thread;
    }


    @Override
    public <T extends ILoopTaskExecutor> T getTaskExecutor() {
        return (T) objectExecutor;
    }

    @Override
    public <T extends BaseLoopTask> T getTask() {
        return (T) task;
    }

    @Override
    public void release() {
        objectExecutor = null;
        thread = null;
        task = null;
    }

}
