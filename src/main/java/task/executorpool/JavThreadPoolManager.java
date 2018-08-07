package task.executorpool;


import task.executor.BaseConsumerTask;
import task.executor.BaseLoopTask;
import task.executor.TaskContainer;
import task.executor.interfaces.ITaskContainer;
import task.executor.interfaces.ITaskExecutor;
import task.executorpool.interfaces.IThreadPoolManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程池
 *
 * @author YDZ
 */
public class JavThreadPoolManager implements IThreadPoolManager {
    private static JavThreadPoolManager pool = null;
    /**
     * 缓存线程栈
     */
    private static Queue<ITaskContainer> containerCache = new ConcurrentLinkedQueue();

    private JavThreadPoolManager() {
        DestroyTask recycleTask = new DestroyTask();
        Thread thread = new Thread(recycleTask);
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public synchronized static JavThreadPoolManager getInstance() {
        if (pool == null) {
            synchronized (JavThreadPoolManager.class) {
                pool = new JavThreadPoolManager();
            }
        }
        return pool;
    }

    public static ITaskContainer createJThread(BaseLoopTask loopTask) {
        TaskContainer thread = new TaskContainer(loopTask);
        containerCache.add(thread);
        return thread;
    }

    public static ITaskContainer createJThread(BaseConsumerTask consumerTask) {
        TaskContainer thread = new TaskContainer(consumerTask);
        containerCache.add(thread);
        return thread;
    }

    private ITaskContainer getTaskContainer() {
        if (containerCache.size() > 0) {
            for (ITaskContainer container : containerCache) {
                boolean isIdleState = container.getTaskExecutor().isIdleState();
                if (isIdleState) {
                    return container;
                }
            }
        }
        return null;
    }


    @Override
    public void addTask(BaseLoopTask loopTask) {
        ITaskContainer taskContainer = getTaskContainer();
        if (taskContainer == null) {
            taskContainer = new TaskContainer(loopTask);
            taskContainer.getTaskExecutor().startTask();
            containerCache.add(taskContainer);
        } else {
            taskContainer.getTaskExecutor().changeTask(loopTask);
        }
    }

    @Override
    public void addTask(BaseConsumerTask consumerTask) {
        addTask((BaseLoopTask) consumerTask);
    }

    @Override
    public void removeTask(BaseLoopTask loopTask) {

    }

    @Override
    public void removeTask(BaseConsumerTask loopTask) {

    }

    @Override
    public void recycleThread(ITaskContainer container) {
        ITaskExecutor executor = container.getTaskExecutor();
        boolean state = executor.getMultiplexState() && executor.isIdleState();
        if (state) {
            containerCache.add(container);
        }
    }

    @Override
    public final boolean changeTask(ITaskContainer container, BaseLoopTask newTask) {
        ITaskExecutor executor = container.getTaskExecutor();
        return executor.changeTask(newTask);
    }


    @Override
    public void destroy(ITaskContainer container) {
        ITaskExecutor executor = container.getTaskExecutor();
        executor.destroyTask();
    }

    @Override
    public boolean isIdleState(ITaskContainer container) {
        return container.getTaskExecutor().isIdleState();
    }

    @Override
    public void destroyAll() {
        for (ITaskContainer container : containerCache) {
            ITaskExecutor executor = container.getTaskExecutor();
            executor.destroyTask();
        }
        containerCache.clear();
    }

    private class DestroyTask implements Runnable {

        @Override
        public void run() {
            destroyAll();
        }
    }

}