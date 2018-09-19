package task.cache.joggle;

import java.io.Serializable;

public interface ICache {

    <T extends Serializable> T read(String key);

    void write(String key, Serializable data);

    void readAsync(String key, ICacheResultListener<Serializable> listener);

    void writeAsync(String key, Serializable data, ICacheResultListener<Serializable> listener);
}
