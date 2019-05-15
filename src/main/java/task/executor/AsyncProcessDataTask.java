package task.executor;

import task.executor.joggle.ILoopTaskExecutor;

/**
 * 异步处理数据线程
 */
public class AsyncProcessDataTask extends BaseLoopTask {

    private ConsumerEngine mCoreTask;
    private ILoopTaskExecutor mAsyncTaskExecutor;

    public AsyncProcessDataTask(ConsumerEngine coreTask) {
        this.mCoreTask = coreTask;
    }

    @Override
    public void onRunLoopTask() {
//        if (mCoreTask.getAttribute() != null && mCoreTask.getAttribute().getCacheDataSize() > 0) {
            mCoreTask.onProcess();
//        } else {
//            mAsyncTaskExecutor.waitTask(0);
//        }
    }

    public ILoopTaskExecutor startTask() {
        TaskContainer container = new TaskContainer(this);
        mAsyncTaskExecutor = container.getTaskExecutor();
        mAsyncTaskExecutor.startTask();
        return mAsyncTaskExecutor;
    }

}
