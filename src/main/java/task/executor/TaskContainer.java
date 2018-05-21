package task.executor;


import com.yyz.CurrencyThreadTask.task.executor.interfaces.ILoopTaskExecutor;
import com.yyz.CurrencyThreadTask.task.executor.interfaces.ITaskContainer;

/**
 * 线程实体
 *
 * @author yyz
 */
public class TaskContainer implements ITaskContainer {

    /**
     * 要执行的任务
     */
    private BaseLoopTask task = null;
    /**
     * 线程体
     */
    private Thread thread = null;
    /**
     * 执行体
     */
    private LoopTaskExecutor objectExecutor = null;


    /**
     * 创建生产消费处理任务
     *
     * @param task 普通任务
     */
    public <D> TaskContainer(BaseConsumerTask<D> task) {
        this(task, task.getClass().getName());
    }

    /**
     * 创建生产消费处理任务
     *
     * @param task       普通任务
     * @param threadName 任务名
     */
    public <D> TaskContainer(BaseConsumerTask<D> task, String threadName) {
        if (task == null) {
            throw new NullPointerException("task is null");
        } else {
            this.task = task;
            ConsumerTaskExecutor<D> executor = new ConsumerTaskExecutor(this);
            thread = new Thread(executor.getRunnable(), threadName);
            objectExecutor = executor;
        }
    }


    /**
     * 创建循环任务任务
     *
     * @param task 循环任务
     */
    public TaskContainer(BaseLoopTask task) {
        this(task, task.getClass().getName());
    }

    public TaskContainer( BaseLoopTask task, String threadName) {
        if (task == null) {
            throw new NullPointerException("task is null");
        } else {
            this.task = task;
            LoopTaskExecutor taskExecutor = new LoopTaskExecutor(this);
            thread = new Thread(taskExecutor.getRunnable(), threadName);
            objectExecutor = taskExecutor;
        }
    }

    /**
     * 创建socket通讯任务
     *
     * @param task socket任务
     */
    public <D> TaskContainer(BaseSocketTask<D> task) {
        this(task, task.getClass().getName());
    }

    /**
     * 创建socket通讯任务
     *
     * @param task
     * @param threadName socket任务名
     */
    public <D> TaskContainer( BaseSocketTask<D> task,  String threadName) {
        if (task == null) {
            throw new NullPointerException("task is null");
        } else {
            this.task = task;
            SocketTaskExecutor<D> executor = new SocketTaskExecutor(this);
            thread = new Thread(executor.getRunnable(), threadName);
            objectExecutor = executor;
        }
    }


    @Override
    public Thread getNewThread() {
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
    protected void finalize() throws Throwable {
        task = null;
        thread = null;
        objectExecutor = null;
        super.finalize();
    }
}
