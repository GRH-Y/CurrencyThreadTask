package task.executor;


import log.LogDog;
import task.executor.joggle.IAttribute;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;
import task.executor.joggle.IThreadPoolManager;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程池
 *
 * @author YDZ
 */
public class TaskExecutorPoolManager implements IThreadPoolManager {

    /**
     * 缓存线程栈
     */
    private ConcurrentLinkedQueue<ITaskContainer> containerCache = new ConcurrentLinkedQueue();

    private TaskExecutorPoolManager() {
    }

    private static class Inner {
        private static final TaskExecutorPoolManager pool = new TaskExecutorPoolManager();
    }

    public static TaskExecutorPoolManager getInstance() {
        return Inner.pool;
    }

    private ITaskContainer getTaskContainer(BaseLoopTask task) {
        if (!containerCache.isEmpty()) {
            synchronized (containerCache) {
                for (ITaskContainer container : containerCache) {
                    ILoopTaskExecutor executor = container.getTaskExecutor();
                    boolean isIdleState = executor.isIdleState();
                    if (isIdleState) {
                        if (executor instanceof ConsumerTaskExecutor && task instanceof BaseConsumerTask) {
                            return container;
                        } else if (!(executor instanceof ConsumerTaskExecutor) && !(task instanceof BaseConsumerTask)) {
                            return container;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ITaskContainer createLoopTask(BaseLoopTask loopTask, IAttribute attribute) {
        return createPoolTask(loopTask, null, attribute);
    }

    public ITaskContainer createConsumerTask(BaseConsumerTask consumerTask, IAttribute attribute) {
        return createPoolTask(null, consumerTask, attribute);
    }

    private ITaskContainer createPoolTask(BaseLoopTask loopTask, BaseConsumerTask consumerTask, IAttribute attribute) {
        BaseLoopTask execTask = loopTask == null ? consumerTask : loopTask;
        ITaskContainer taskContainer = getTaskContainer(execTask);
        if (taskContainer != null) {
            changeTask(taskContainer, execTask, attribute);
        } else {
            taskContainer = new TaskContainer(execTask);
            taskContainer.getTaskExecutor().setAttribute(attribute);
            taskContainer.getTaskExecutor().setMultiplexTask(true);
            containerCache.add(taskContainer);
        }
        return taskContainer;
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
            taskContainer.getTaskExecutor().setAttribute(attribute);
            taskContainer.getTaskExecutor().setMultiplexTask(true);
            taskContainer.getTaskExecutor().startTask();
            containerCache.add(taskContainer);
        } else {
            taskContainer.getTaskExecutor().changeTask(execTask);
        }
        return taskContainer;
    }


    @Override
    public void closeTask(BaseLoopTask loopTask) {
        for (ITaskContainer container : containerCache) {
            LoopTaskExecutor taskExecutor = container.getTaskExecutor();
            if (taskExecutor.executorTask == loopTask) {
                taskExecutor.blockStopTask();
                return;
            }
        }
    }

    @Override
    public void closeTask(BaseConsumerTask consumerTask) {
        for (ITaskContainer container : containerCache) {
            ConsumerTaskExecutor taskExecutor = container.getTaskExecutor();
            if (taskExecutor.executorTask instanceof ConsumerEngine) {
                ConsumerEngine coreTask = (ConsumerEngine) taskExecutor.executorTask;
                if (coreTask.getTask() == consumerTask) {
                    taskExecutor.blockStopTask();
                    return;
                }
            }
        }
    }

    @Override
    public void multiplexThread(ITaskContainer container) {
        if (container == null || container.getTaskExecutor() == null) {
            LogDog.e("## multiplexThread() container or  container.getTaskExecutor()  is null !!! ");
        }
        ILoopTaskExecutor executor = container.getTaskExecutor();
        boolean state = executor.isMultiplexState() && executor.isIdleState();
        if (state) {
            containerCache.add(container);
        }
    }

    @Override
    public final boolean changeTask(ITaskContainer container, BaseLoopTask newTask, IAttribute attribute) {
        if (container == null || container.getTaskExecutor() == null || newTask == null) {
            LogDog.e("## changeTask() container container.getTaskExecutor() or newTask is null !!! ");
            return false;
        }
        ILoopTaskExecutor executor = container.getTaskExecutor();
        executor.setAttribute(attribute);
        return executor.changeTask(newTask);
    }


    @Override
    public void destroy(ITaskContainer container) {
        if (container != null) {
            ILoopTaskExecutor executor = container.getTaskExecutor();
            if (executor != null) {
                executor.destroyTask();
            }
            container.release();
            containerCache.remove(container);
        }
    }

    @Override
    public boolean isIdleState(ITaskContainer container) {
        if (container == null || container.getTaskExecutor() == null) {
            LogDog.e("## isIdleState() container or container.getTaskExecutor() is null !!! ");
            return false;
        }
        return container.getTaskExecutor().isIdleState();
    }

    @Override
    public void destroyAll() {
        for (ITaskContainer container : containerCache) {
            ILoopTaskExecutor executor = container.getTaskExecutor();
            if (executor != null) {
                executor.destroyTask();
            }
            container.release();
        }
        containerCache.clear();
    }
}