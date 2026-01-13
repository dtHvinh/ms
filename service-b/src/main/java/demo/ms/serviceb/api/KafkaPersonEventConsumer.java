package demo.ms.serviceb.api;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import demo.ms.common.Json;
import demo.ms.common.PersonEvent;

public final class KafkaPersonEventConsumer implements AutoCloseable {
    private final Consumer<String, String> consumer;
    private final String topic;
    private final PersonEventHandler handler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread thread;

    public KafkaPersonEventConsumer(String bootstrapServers, String groupId, String topic, PersonEventHandler handler) {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            throw new IllegalArgumentException("bootstrapServers is required");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("groupId is required");
        }
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic is required");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler is required");
        }
        this.topic = topic;
        this.handler = handler;

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "true");

        this.consumer = new KafkaConsumer<>(props);
    }

    KafkaPersonEventConsumer(Consumer<String, String> consumer, String topic, PersonEventHandler handler) {
        this.consumer = consumer;
        this.topic = topic;
        this.handler = handler;
    }

    public void start() {
        start(null);
    }

    void start(Runnable afterSubscribe) {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        consumer.subscribe(Collections.singletonList(topic));
        if (afterSubscribe != null) {
            afterSubscribe.run();
        }
        thread = new Thread(this::runLoop, "kafka-person-consumer");
        thread.setDaemon(true);
        thread.start();
    }

    private void runLoop() {
        while (running.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(250));
            for (ConsumerRecord<String, String> record : records) {
                try {
                    PersonEvent event = Json.fromJson(record.value(), PersonEvent.class);
                    handler.handle(event);
                } catch (Exception ignored) {
                    // ignore bad messages
                }
            }
        }
    }

    @Override
    public void close() {
        running.set(false);
        try {
            if (thread != null) {
                thread.join(1000);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                consumer.wakeup();
            } catch (Exception ignored) {
            }
            consumer.close();
        }
    }
}
