package task.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RwLockFactory {
    private Map<String, RwLockEntity> lockEntityMap = new ConcurrentHashMap<>();

    public void tryReadLock(String base64Key) {

    }

    public void tryWriteLock(String base64Key) {

    }

    public void unlockMemory(String base64Key) {
        RwLockEntity entity = lockEntityMap.get(base64Key);
        if (entity != null) {
            entity.unlockMemory();
            if (!entity.isMemoryLocked() && !entity.isFileLocked()) {
                lockEntityMap.remove(base64Key);
            }
        }
    }

    public void unlockFile(String base64Key) {
        RwLockEntity entity = lockEntityMap.get(base64Key);
        if (entity != null) {
            entity.unlockFile();
            if (!entity.isMemoryLocked() && !entity.isFileLocked()) {
                lockEntityMap.remove(base64Key);
            }
        }
    }


    public void lockMemory(String base64Key) {
        RwLockEntity entity = lockEntityMap.get(base64Key);
        if (entity == null) {
            //说明该锁是安全的
            synchronized (RwLockFactory.class) {
                if (!lockEntityMap.containsKey(base64Key)) {
                    entity = new RwLockEntity();
                    entity.lockMemory();
                    lockEntityMap.put(base64Key, entity);
                }
            }
        } else {
            //有其他地方正在使用该锁
            while (entity.isMemoryLocked()) {
                entity.waitMemory();
            }
            //保存锁
            entity.lockMemory();
            if (!lockEntityMap.containsKey(base64Key)) {
                lockEntityMap.put(base64Key, entity);
            }
        }
    }

    public void lockFile(String base64Key) {
        RwLockEntity entity = lockEntityMap.get(base64Key);
        if (entity == null) {
            //说明该锁是安全的
            synchronized (RwLockFactory.class) {
                if (!lockEntityMap.containsKey(base64Key)) {
                    entity = new RwLockEntity();
                    entity.lockFile();
                    lockEntityMap.put(base64Key, entity);
                }
            }
        } else {
            //有其他地方正在使用该锁
            while (entity.isFileLocked()) {
                entity.waitFile();
            }
            //保存锁
            entity.lockFile();
            if (!lockEntityMap.containsKey(base64Key)) {
                lockEntityMap.put(base64Key, entity);
            }
        }
    }

}
