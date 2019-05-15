package task.executor;


import task.executor.joggle.IAttribute;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;
import task.executor.joggle.IThreadPoolManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程池
 *
 * @author YDZ
 */
public class TaskExecutorPoolManager implements IThreadPoolManager {
    private static TaskExecutorPoolManager pool = null;
    /**
     * 缓存线程栈
     */
    private static Queue<ITaskContainer> containerCache = new ConcurrentLinkedQueue();

    private TaskExecutorPoolManager() {
        DestroyTask recycleTask = new DestroyTask();
        Thread thread = new Thread(recycleTask);
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public static synchronized TaskExecutorPoolManager getInstance() {
        if (pool == null) {
            synchronized (TaskExecutorPoolManager.class) {
                pool = new TaskExecutorPoolManager();
            }
        }
        return pool;
    }

    private ITaskContainer getTaskContainer(BaseLoopTask task) {
        if (!containerCache.isEmpty()) {
            for (ITaskContainer container : containerCache) {
                ILoopTaskExecutor executor = container.getTaskExecutor();
                boolean isIdleState = executor.isIdleState();
                if (isIdleState) {
                    if (executor instanceof ConsumerTaskExecutor && task instanceof BaseConsumerTask) {
                        return container;
                    } else if (executor instanceof LoopTaskExecutor && !(task instanceof BaseConsumerTask)) {
                        return container;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ITaskContainer createJThread(BaseLoopTask loopTask) {
        TaskContainer thread = new TaskContainer(loopTask);
        thread.getTaskExecutor().setMultiplexTask(true);
        containerCache.add(thread);
        return thread;
    }

    public ITaskContainer createJThread(BaseConsumerTask consumerTask) {
        TaskContainer thread = new TaskContainer(consumerTask);
        thread.getTaskExecutor().setMultiplexTask(true);
        containerCache.add(thread);
        return thread;
    }


    @Override
    public ITaskContainer runTask(BaseLoopTask loopTask, IAttribute attribute) {
        return runTask(loopTask, null, attribute);
    }

    @Override
    public ITaskContainer runTask(BaseConsumerTask consumerTask, IAttribute attribute) {
        return runTask(null, consumerTask, attribute);
    }

    private ITaskContainer runTask(BaseLoopTask loopTask, BaseConsumerTask consumerTask, IAttribute attribute) {
        if (loopTask == null && consumerTask == null) {
            return null;
        }
        BaseLoopTask execTask = loopTask == null ? consumerTask : loopTask;
        ITaskContainer taskContainer = getTaskContainer(execTask);
        if (taskContainer == null) {
            taskContainer = new TaskContainer(execTask);
            taskContainer.setAttribute(attribute);
            taskContainer.getTaskExecutor().setMultiplexTask(true);
            taskContainer.getTaskExecutor().startTask();
            containerCache.add(taskContainer);
        } else {
            taskContainer.getTaskExecutor().changeTask(execTask);
        }
        return taskContainer;
    }


    @Override
    public void removeTask(BaseLoopTask loopTask) {
        for (ITaskContainer container : containerCache) {
            LoopTaskExecutor taskExecutor = container.getTaskExecutor();
            if (taskExecutor.executorTask == loopTask) {
                taskExecutor.stopTask();
                return;
            }
        }
    }

    @Override
    public void removeTask(BaseConsumerTask consumerTask) {
        for (ITaskContainer container : containerCache) {
            ConsumerTaskExecutor taskExecutor = container.getTaskExecutor();
            if (taskExecutor.executorTask instanceof ConsumerEngine) {
                ConsumerEngine coreTask = (ConsumerEngine) taskExecutor.executorTask;
                if (coreTask.getTask() == consumerTask) {
                    taskExecutor.stopTask();
                    return;
                }
            }
        }
    }

    @Override
    public void recycleThread(ITaskContainer container) {
        ILoopTaskExecutor executor = container.getTaskExecutor();
        boolean state = executor.getMultiplexState() && executor.isIdleState();
        if (state) {
            containerCache.add(container);
        }
    }

    @Override
    public final boolean changeTask(ITaskContainer container, BaseLoopTask newTask) {
        ILoopTaskExecutor executor = container.getTaskExecutor();
        return executor.changeTask(newTask);
    }


    @Override
    public void destroy(ITaskContainer container) {
        ILoopTaskExecutor executor = container.getTaskExecutor();
        executor.destroyTask();
    }

    @Override
    public boolean isIdleState(ITaskContainer container) {
        return container.getTaskExecutor().isIdleState();
    }

    @Override
    public void destroyAll() {
        for (ITaskContainer container : containerCache) {
            ILoopTaskExecutor executor = container.getTaskExecutor();
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