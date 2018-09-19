package task.cache;

import task.cache.joggle.ICache;
import task.cache.joggle.ICacheResultListener;

import java.io.Serializable;
import java.util.Base64;

public class CacheManger implements ICache {

    private CacheMemory cacheMemory;
    private CacheFile cacheFile;
    private RwLockFactory rwLockFactory;

    public CacheManger(String cachePath) {
        cacheMemory = new CacheMemory();
        cacheFile = new CacheFile(cachePath);
        rwLockFactory = new RwLockFactory();
    }


    @Override
    public <T extends Serializable> T read(String key) {
        String base64 = Base64.getEncoder().encodeToString(key.getBytes());
        rwLockFactory.lockMemory(base64);
        T data = cacheMemory.read(base64);
        rwLockFactory.unlockMemory(base64);
        if (data == null) {
            rwLockFactory.lockFile(base64);
            data = cacheFile.read(base64);
            rwLockFactory.unlockFile(base64);
        }
        return data;
    }

    @Override
    public void write(String key, Serializable data) {
        String base64 = Base64.getEncoder().encodeToString(key.getBytes());
        if (!cacheMemory.containsKey(base64)) {
            rwLockFactory.lockMemory(base64);
            cacheMemory.write(base64, data);
            rwLockFactory.unlockMemory(base64);
        }
        if (!cacheFile.containsKey(base64)) {
            rwLockFactory.lockFile(base64);
            cacheFile.write(base64, data);
            rwLockFactory.unlockFile(base64);
        }
    }

    @Override
    public void readAsync(String key, ICacheResultListener<Serializable> listener) {

    }

    @Override
    public void writeAsync(String key, Serializable data, ICacheResultListener<Serializable> listener) {

    }
}
