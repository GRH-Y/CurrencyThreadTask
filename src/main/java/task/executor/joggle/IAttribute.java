package task.executor.joggle;

public interface IAttribute<T>  {

    /**
     * 获取缓存集合
     *
     * @return
     */
    <T> T getCache();

    /**
     * 获取当前缓存区数据的数量
     *
     * @return 返回数据数量
     */
    int getCacheDataSize();

    /**
     * 清除缓存区所有的数据
     */
    void clearCacheData();

    /**
     * 设置缓存区最大的数据数量
     *
     * @param count 最大数量
     */
    void setCacheMaxCount(int count);

    /**
     * 设置存储数据模式
     *
     * @param isCrowdOut true 则缓冲区满时挤掉最早的数据，false为缓冲区满则不保存
     */
    void setPushDataModel(boolean isCrowdOut);

    /**
     * 往缓存区添加数据
     *
     * @param data 数据
     */
    void pushToCache(T data);


    /**
     * 获取数据栈中的数据
     *
     * @return 返回栈低的数据
     */
    T popCacheData();

}
