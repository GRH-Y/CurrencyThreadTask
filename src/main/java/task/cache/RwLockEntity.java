package task.cache;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RwLockEntity {
    private ReentrantLock memoryLock = new ReentrantLock(true);
    private Condition memoryCondition = memoryLock.newCondition();

    private ReentrantLock fileLock = new ReentrantLock(true);
    private Condition fileCondition = fileLock.newCondition();

    public boolean isFileLocked() {
        return fileLock.isLocked();
    }

    public boolean isMemoryLocked() {
        return memoryLock.isLocked();
    }

    public void lockMemory() {
        memoryLock.lock();
    }

    public  void unlockMemory() {
        memoryCondition.signal();
        memoryLock.unlock();
    }

    public void lockFile() {
        fileLock.lock();
    }

    public  void unlockFile() {
        fileCondition.signal();
        fileLock.unlock();
    }

    public void waitMemory() {
        try {
            memoryCondition.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitFile() {
        try {
            fileCondition.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
