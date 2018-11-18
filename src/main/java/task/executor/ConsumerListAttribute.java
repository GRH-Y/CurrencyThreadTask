package task.executor;

import task.executor.joggle.IConsumerAttribute;

import java.util.LinkedList;
import java.util.List;

/**
 * 非线程安全的缓存
 *
 * @param <D>
 */
public class ConsumerListAttribute<D> implements IConsumerAttribute<D> {
    /**
     * 设置缓存区的大小
     * (特别注意:如果设置为小于1则存储为无限，当大于0时，如果当前缓存区数据大于该值，则会根据crowdOutModel来处理)
     */
    private int mCacheMaxCount = 0;

    /***缓冲区*/
    private final List<D> mList;
//    private final ConcurrentLinkedDeque<D> mList = new ConcurrentLinkedDeque<>();

    /***true 则缓冲区满时挤掉最早的数据，false为缓冲区满则不保存*/
    private boolean crowdOutModel = false;

    public ConsumerListAttribute() {
        mList = new LinkedList<>();
    }

    public ConsumerListAttribute(List<D> list) {
        mList = list;
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
        if (!mList.isEmpty()) {
            data = mList.remove(0);
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
        if (mCacheMaxCount > 0) {
            if (mList.size() >= mCacheMaxCount && crowdOutModel) {
                mList.remove(0);
                mList.add(data);
            }
        } else {
            mList.add(data);
        }
    }

    /**
     * 获取同步任务当前缓冲区的大小
     *
     * @return 返回缓冲区大小
     */
    @Override
    public int getCacheDataSize() {
        return mList.size();
    }

    @Override
    public List<D> getCache() {
        return mList;
    }

    /**
     * 清理所有缓存要发送的数据
     */
    @Override
    public void clearCacheData() {
        mList.clear();
    }

}
