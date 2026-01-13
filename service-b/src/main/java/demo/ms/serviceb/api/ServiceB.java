package demo.ms.serviceb.api;

public final class ServiceB implements AutoCloseable {
    private final RedisPersonRepository repository;
    private final KafkaPersonEventConsumer consumer;

    public ServiceB(RedisPersonRepository repository, KafkaPersonEventConsumer consumer) {
        this.repository = repository;
        this.consumer = consumer;
    }

    public static ServiceB startFromEnv() {
        return startFromEnv(true);
    }

    static ServiceB startFromEnv(boolean startConsumer) {
        String kafka = Env.get("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
        String topic = Env.get("KAFKA_TOPIC", "person-events");
        String groupId = Env.get("KAFKA_GROUP_ID", "service-b");

        String redisHost = Env.get("REDIS_HOST", "redis");
        int redisPort = Env.getInt("REDIS_PORT", 6379);

        JedisKeyValueStore kv = new JedisKeyValueStore(redisHost, redisPort);
        RedisPersonRepository repo = new RedisPersonRepository(kv);
        PersonEventHandler handler = new PersonEventHandler(repo);
        KafkaPersonEventConsumer consumer = new KafkaPersonEventConsumer(kafka, groupId, topic, handler);
        if (startConsumer) {
            consumer.start();
        }
        return new ServiceB(repo, consumer);
    }

    @Override
    public void close() {
        try {
            consumer.close();
        } finally {
            repository.close();
        }
    }
}
