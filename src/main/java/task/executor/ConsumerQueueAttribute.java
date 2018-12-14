package task.executor;

import task.executor.joggle.IConsumerAttribute;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程安全的缓存
 *
 * @param <D>
 */
public class ConsumerQueueAttribute<D> implements IConsumerAttribute<D> {
    /**
     * 设置缓存区的大小
     * (特别注意:如果设置为小于1则存储为无限，当大于0时，如果当前缓存区数据大于该值，则会根据crowdOutModel来处理)
     */
    private int mCacheMaxCount = 0;

    /***缓冲区*/
    private final Queue<D> mCache;

    /***true 则缓冲区满时挤掉最早的数据，false为缓冲区满则不保存*/
    private boolean crowdOutModel = false;

    public ConsumerQueueAttribute() {
        mCache = new ConcurrentLinkedQueue();
    }

    public ConsumerQueueAttribute(Queue<D> queue) {
        mCache = queue;
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
            try {
                data = mCache.remove();
            } catch (Exception e) {
            }
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
        if (data == null) {
            return;
        }
        if (mCacheMaxCount > 0 && mCache.size() >= mCacheMaxCount) {
            if (crowdOutModel) {
                try {
                    mCache.remove();
                } catch (Exception e) {
                }
                mCache.add(data);
            }
        } else {
            mCache.add(data);
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
        mCache.clear();
    }

}
