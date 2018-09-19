package task.cache;

import task.cache.joggle.ICache;
import task.cache.joggle.ICacheResultListener;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存缓存
 */
public class CacheMemory implements ICache {

    private Map<String, Object> mLruCache = new ConcurrentHashMap<>();

    public boolean containsKey(String base64Key) {
        return mLruCache.containsKey(base64Key);
    }

    @Override
    public <T extends Serializable> T read(String base64Key) {
        return (T) mLruCache.get(base64Key);
    }

    @Override
    public void write(String base64Key, Serializable data) {
        mLruCache.put(base64Key, data);
    }

    @Override
    public void readAsync(String key, ICacheResultListener<Serializable> listener) {

    }

    @Override
    public void writeAsync(String key, Serializable data, ICacheResultListener<Serializable> listener) {

    }
}
