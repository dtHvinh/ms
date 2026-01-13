package demo.ms.serviceb.api;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class JedisKeyValueStore implements KeyValueStore {
    private final JedisPool pool;

    public JedisKeyValueStore(String host, int port) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("redis host is required");
        }
        this.pool = new JedisPool(host, port);
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public void set(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
        }
    }

    @Override
    public void del(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        }
    }

    @Override
    public void close() {
        pool.close();
    }
}
