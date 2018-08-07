package task.utils;

import java.util.ArrayList;
import java.util.List;


/**
 * MultiplexCache复用缓存
 * Created by dell on 9/12/2017.
 */

public class MultiplexCache<T> {
    private int popIndex = 0;
    private List<CacheItem<T>> cache;
    private List<CacheItem<T>> useCache;
    private final int FAIL = -1;
    private boolean isRelease = false;

    public MultiplexCache(int number) {
        number = number < 1 ? 10 : number;
        cache = new ArrayList<>(number);
        useCache = new ArrayList<>(number);
    }

    /**
     * 获取当前缓存区可用复用数据数量
     *
     * @return
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * 添加复用的数据
     *
     * @param data 复用数据
     */
    public synchronized void setRepeatData(T data) {
        if (data == null) {
            return;
        }
        int index = contains(useCache, data);
        if (useCache != null && index != FAIL) {
            //该数据块是已存在的
            CacheItem<T> item = useCache.get(index);
            item.lock = false;
            useCache.remove(index);
        } else {
            if (cache != null && contains(cache, data) == FAIL) {
                CacheItem item = new CacheItem<>();
                item.data = data;
                cache.add(item);
            }
        }
    }

    /**
     * 查找数据块是否存在list中
     *
     * @param list
     * @param data
     * @return
     */
    private int contains(List<CacheItem<T>> list, T data) {
        if (list != null && data != null) {
            int index = 0;
            for (CacheItem<T> tmp : list) {
                if (tmp.data == data) {
                    return index;
                }
                index++;
            }
        }
        return FAIL;
    }


    /**
     * 获取可用的数据块(还想复用该数据块需要调用pushData())
     *
     * @return 没有可用数据则返回null
     */
    public synchronized T getRepeatData() {
        if (cache != null && cache.size() > 0) {
            popIndex = popIndex >= cache.size() ? 0 : popIndex;
            CacheItem<T> item = cache.get(popIndex);
            if (item.lock) {
                item = getCanUseData();
            }
            item.lock = true;
            useCache.add(item);//记录正在使用的item
            popIndex++;
            return item.data;
        }
        return null;
    }

    /**
     * 获取没有正在使用的数据块
     *
     * @return
     */
    private CacheItem<T> getCanUseData() {
        CacheItem<T> item = null;
        for (CacheItem<T> tmp : cache) {
            if (!isRelease) {
                break;
            }
            if (!tmp.lock) {
                item = tmp;
                break;
            }
        }
        return item;
    }

    @Override
    protected void finalize() throws Throwable {
        isRelease = true;
        release();
        super.finalize();
    }

    /**
     * 释放资源
     */
    public synchronized void release() {
        isRelease = true;
        if (cache != null) {
            for (int index = 0; index < cache.size(); index++) {
                CacheItem item = cache.get(index);
                item.data = null;
            }
            cache.clear();
            cache = null;
        }
        if (useCache != null) {
            useCache.clear();
            useCache = null;
        }
    }


    private class CacheItem<T> {
        public boolean lock = false;
        public T data = null;

        @Override
        protected void finalize() throws Throwable {
            release();
            super.finalize();
        }

        public void release() {
            data = null;
        }
    }
}
