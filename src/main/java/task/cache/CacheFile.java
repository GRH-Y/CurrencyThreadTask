package task.cache;


import task.cache.joggle.ICache;
import task.cache.joggle.ICacheResultListener;
import util.StringUtils;

import java.io.File;
import java.io.Serializable;

/**
 * 文件缓存
 */
public class CacheFile implements ICache {

    private String mCachePath;

    public CacheFile(String cachePath) {
        if (StringUtils.isEmpty(cachePath)) {
            throw new NullPointerException("cachePath is null !");
        }
        File file = new File(cachePath);
        boolean exists = file.exists();
        if (!exists) {
            exists = file.mkdirs();
        }
        if (!exists) {
            throw new IllegalStateException("Failed to create directory = " + cachePath);
        }
        this.mCachePath = cachePath;
    }

    public boolean containsKey(String base64Key) {
        return new File(mCachePath, base64Key).exists();
    }

    @Override
    public <T extends Serializable> T read(String base64Key) {
        return (T) SerializableUtils.readFile(mCachePath, base64Key);
    }

    @Override
    public void write(String base64Key, Serializable data) {
        SerializableUtils.writeFile(mCachePath, base64Key, data);
    }

    @Override
    public void readAsync(String key, ICacheResultListener<Serializable> listener) {

    }

    @Override
    public void writeAsync(String key, Serializable data, ICacheResultListener<Serializable> listener) {

    }
}
