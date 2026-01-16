package com.dthvinh.libs.kafka.common;

public final class Env {
    public static final String KAFKA_BOOTSTRAP_SERVER = System.getenv("KAFKA_BOOTSTRAP_SERVER");
    public static final String REDIS_PORT = System.getenv("REDIS_PORT");
    public static final String REDIS_HOST = System.getenv("REDIS_HOST");
}
