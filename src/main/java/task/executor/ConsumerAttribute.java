package task.executor;

import task.executor.interfaces.IConsumerAttribute;
import task.executor.interfaces.ITaskExecutor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @param <D>
 */
public class ConsumerAttribute<D> implements IConsumerAttribute<D> {
    /**
     * 设置缓存区的大小
     * (特别注意:如果设置为小于1则存储为无限，当大于0时，如果当前缓存区数据大于该值，则会根据crowdOutModel来处理)
     */
    private int mCacheMaxCount = 0;

    /***缓冲区*/
    private final Queue<D> mCache = new ConcurrentLinkedQueue();

    /***true 则缓冲区满时挤掉最早的数据，false为缓冲区满则不保存*/
    private boolean crowdOutModel = false;

    private ConsumerTaskExecutor mTaskExecutor;

    /***异步处理数据任务*/
    private ITaskExecutor asyncTaskExecutor = null;

    public ConsumerAttribute(ConsumerTaskExecutor taskExecutor) {
        this.mTaskExecutor = taskExecutor;
    }

    /**
     * 设置缓存区数量
     *
     * @param count 数量，默认是0无限大
     */
    @Override
    public void setCacheMaxCount(int count) {
        this.mCacheMaxCount = count;
    }


    /**
     * 设置存储数据模式
     *
     * @param isCrowdOut true 则缓冲区满时挤掉最早的数据，false为缓冲区满则不保存
     */
    @Override
    public void setPushDataModel(boolean isCrowdOut) {
        this.crowdOutModel = isCrowdOut;
    }

    /**
     * 获取数据栈中的数据
     *
     * @return 返回栈低的数据
     */
    @Override
    public D popCacheData() {
        D data = null;
        if (!mCache.isEmpty()) {
            data = mCache.remove();
        }
        return data;
    }

    /**
     * 把数据压入数据栈
     *
     * @param data 数据
     */
    @Override
    public void pushToCache(D data) {
        if (!mTaskExecutor.getLoopState() || data == null) {
            return;
        }
        if (mCacheMaxCount > 0) {
            if (mCache.size() >= mCacheMaxCount && crowdOutModel) {
                mCache.remove(0);
                mCache.add(data);
            }
        } else {
            mCache.add(data);
        }
        if (asyncTaskExecutor != null) {
            asyncTaskExecutor.resumeTask();
        } else {
            mTaskExecutor.resumeTask();
        }
    }

    /**
     * 获取同步任务当前缓冲区的大小
     *
     * @return 返回缓冲区大小
     */
    @Override
    public int getCacheDataSize() {
        return mCache.size();
    }

    @Override
    public Queue<D> getCache() {
        return mCache;
    }

    /**
     * 清理所有缓存要发送的数据
     */
    @Override
    public void clearCacheData() {
        if (!mTaskExecutor.getIdleStopState()) {
            mCache.clear();
        }
    }

    /**
     * 异步处理数据线程
     */
    private class AsyncProcessDataTask extends BaseLoopTask {

        @Override
        public void onRunLoopTask() {
            if (getCacheDataSize() > 0) {
                ((BaseConsumerTask) (mTaskExecutor.executorTask)).onProcess();
            } else {
                asyncTaskExecutor.waitTask(0);
            }
        }
    }


    /**
     * 开启异步处理数据模式,
     * 开启后 onCreateData ,onProcess 分别不同线程来执行
     */
    @Override
    public void startAsyncProcessData() {
        if (asyncTaskExecutor == null) {
            AsyncProcessDataTask asyncTask = new AsyncProcessDataTask();
            TaskContainer container = new TaskContainer(asyncTask);
            asyncTaskExecutor = container.getTaskExecutor();
            asyncTaskExecutor.startTask();
        }
    }

    /**
     * 关闭异步处理数据模式
     */
    @Override
    public void stopAsyncProcessData() {
        if (asyncTaskExecutor != null) {
            asyncTaskExecutor.stopTask();
            asyncTaskExecutor = null;
        }
    }


    /**
     * 当前是否异步处理数据
     *
     * @return
     */
    @Override
    public boolean isAsyncState() {
        return asyncTaskExecutor != null;
    }

}
