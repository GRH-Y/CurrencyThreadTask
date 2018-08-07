package task.executor;


import task.executor.interfaces.IConsumerAttribute;
import task.executor.interfaces.IConsumerTaskExecutor;

/**
 * 消费任务执行者
 * Created by dell on 2/9/2018.
 *
 * @author yyz
 */
public class ConsumerTaskExecutor<D> extends LoopTaskExecutor implements IConsumerTaskExecutor<D> {

    /***没有输任务则休眠标志位，为true则休眠*/
    private boolean isIdleStateSleep = false;

    protected BaseConsumerTask consumerTask;


    /**
     * 创建任务
     *
     * @param container 任务
     * @throws NullPointerException
     */
    protected ConsumerTaskExecutor(TaskContainer container) {
        super(container);
        this.consumerTask = container.getTask();
        loopTask = new CoreTask();

    }

    // -------------------start run ---------------------

    private class CoreTask<D> extends BaseConsumerTask<D> {

        protected IConsumerAttribute<D> attribute;

        CoreTask() {
            attribute = new ConsumerAttribute<>(ConsumerTaskExecutor.this);
        }

        @Override
        protected void onInitTask() {
            consumerTask.onInitTask();
        }

        @Override
        protected void onRunLoopTask() {
            consumerTask.onRunLoopTask();
            onCreateData();
            // 没有数据是否需要线程休眠
            if (attribute.getCacheDataSize() == 0 && isIdleStateSleep) {
                waitTask();
            } else if (!attribute.isAsyncState() && getLoopState()) {
                onProcess();
            }
        }


        @Override
        protected void onCreateData() {
            consumerTask.onCreateData();
        }

        @Override
        protected void onProcess() {
            consumerTask.onProcess();
        }


        @Override
        protected void onIdleStop() {
            consumerTask.onIdleStop();
        }

        @Override
        protected void onDestroyTask() {
            consumerTask.onDestroyTask();
            attribute.clearCacheData();
        }
    }

    // -------------------End run ---------------------


    /**
     * 设置缓存区没有数据线程进入休眠
     *
     * @param state true 进入休眠状态
     */
    @Override
    public void setIdleStateSleep(boolean state) {
        this.isIdleStateSleep = state;
    }


    @Override
    public void stopTask() {
        super.stopTask();
        CoreTask coreTask = (CoreTask) loopTask;
        coreTask.attribute.stopAsyncProcessData();
    }


    /**
     * 获取属性
     *
     * @return
     */
    @Override
    public IConsumerAttribute<D> getAttribute() {
        CoreTask coreTask = (CoreTask) loopTask;
        return coreTask.attribute;
    }
}
