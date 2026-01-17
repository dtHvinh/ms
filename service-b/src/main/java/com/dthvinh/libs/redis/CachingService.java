package com.dthvinh.libs.redis;


import com.dthvinh.libs.kafka.common.Env;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.osgi.service.component.annotations.Component;
import redis.clients.jedis.RedisClient;

import java.lang.reflect.Type;
import java.util.List;

@Component(immediate = true, service = CachingService.class)
public class CachingService {

    private final RedisClient jedis = RedisClient.builder()
            .hostAndPort(Env.REDIS_HOST, Integer.parseInt(Env.REDIS_PORT))
            .build();

    private final Gson gson = new Gson();

    public void cache(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        String json = gson.toJson(value);
        jedis.set(key, json);
    }

    public <T> T get(String key, Class<T> classOfT) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }

        String json = jedis.get(key);
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to deserialize cached value for key: " + key);
            e.printStackTrace();
            return null;
        }
    }

    public <T> List<T> getList(String key, Class<T> innerType) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }

        String json = jedis.get(key);
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            Type listType = TypeToken.getParameterized(List.class, innerType).getType();
            return gson.fromJson(json, listType);
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to deserialize list for key: " + key);
            e.printStackTrace();
            return null;
        }
    }

    public void invalidate(String key) {
        if (key != null && !key.trim().isEmpty()) {
            jedis.del(key);
        }
    }

    public void clearAll() {
        jedis.flushDB();
    }
}