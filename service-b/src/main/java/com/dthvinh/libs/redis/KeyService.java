package com.dthvinh.libs.redis;

import java.util.UUID;

public class KeyService {
    public static String createKey(String id) {
        return "persons:#%s".formatted(id);
    }

    public static String createKey(int id) {
        return "persons:#%s".formatted(id);
    }
    
    public static String createKey(UUID id) {
        return "persons:#%s".formatted(id);
    }
}
