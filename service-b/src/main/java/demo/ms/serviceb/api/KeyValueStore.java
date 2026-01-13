package demo.ms.serviceb.api;

public interface KeyValueStore extends AutoCloseable {
    String get(String key);

    void set(String key, String value);

    void del(String key);

    @Override
    void close();
}
