package task.cache.joggle;

public interface ICacheResultListener<T> {

    void onResult(String key, T readData, boolean isWriteSuccess);
}
