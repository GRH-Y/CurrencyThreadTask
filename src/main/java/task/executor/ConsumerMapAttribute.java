package task.executor;

import task.executor.joggle.IConsumerAttribute;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerMapAttribute<D> implements IConsumerAttribute<D> {

    private final Map<String, D> mMapCache;

    public ConsumerMapAttribute() {
        mMapCache = new ConcurrentHashMap();
    }

    public ConsumerMapAttribute(Map<String, D> map) {
        this.mMapCache = map;
    }

    @Override
    public Map<String, D> getCache() {
        return mMapCache;
    }

    @Override
    public int getCacheDataSize() {
        return mMapCache.size();
    }

    @Override
    public void clearCacheData() {
        mMapCache.clear();
    }

    @Override
    public void setCacheMaxCount(int count) {

    }

    @Override
    public void setPushDataModel(boolean isCrowdOut) {

    }

    @Override
    public void pushToCache(D d) {
        mMapCache.put(d.toString(), d);
    }

    @Override
    public D popCacheData() {
        if (mMapCache.isEmpty()) {
            return null;
        }
        D data = null;
        Set<String> keySet = mMapCache.keySet();
        if (!keySet.isEmpty()) {
            Object[] arg = keySet.toArray();
            synchronized (ConsumerMapAttribute.class) {
                if (!keySet.isEmpty()) {
                    data = mMapCache.remove(arg[arg.length - 1]);
                }
            }
        }
        return data;
    }
}
