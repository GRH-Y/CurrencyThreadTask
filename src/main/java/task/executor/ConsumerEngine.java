package task.executor;

import task.executor.joggle.IConsumerAttribute;

public class ConsumerEngine<D> extends BaseConsumerTask<D> {


    protected BaseConsumerTask mConsumerTask;
    private IConsumerAttribute mAttribute;
    private ConsumerTaskExecutor executor;

    public ConsumerEngine(ConsumerTaskExecutor executor, BaseConsumerTask task) {
        this.mConsumerTask = task;
        this.executor = executor;
    }

    protected synchronized void setAttribute(IConsumerAttribute attribute) {
        this.mAttribute = attribute;
    }

    protected synchronized IConsumerAttribute getAttribute() {
        return mAttribute;
    }

    @Override
    protected void onInitTask() {
        mConsumerTask.onInitTask();
    }

    @Override
    protected void onRunLoopTask() {
        mConsumerTask.onRunLoopTask();
        onCreateData();
        // 没有数据是否需要线程休眠
        if ((mAttribute == null && executor.isIdleStateSleep()) ||
                (mAttribute.getCacheDataSize() == 0 && executor.isIdleStateSleep())) {
            executor.waitTask();
        } else if (executor.getAsyncTaskExecutor() == null && executor.getLoopState()) {
            onProcess();
        }
    }


    @Override
    protected void onCreateData() {
        mConsumerTask.onCreateData();
    }

    @Override
    protected void onProcess() {
        mConsumerTask.onProcess();
    }


    @Override
    protected void onIdleStop() {
        mConsumerTask.onIdleStop();
    }

    @Override
    protected void onDestroyTask() {
        mConsumerTask.onDestroyTask();
        if (!executor.getIdleStopState() && mAttribute != null) {
            mAttribute.clearCacheData();
        }
    }
}
