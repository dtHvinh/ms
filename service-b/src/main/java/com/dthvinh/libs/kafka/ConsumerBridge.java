package com.dthvinh.libs.kafka;

import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerBridge implements Runnable, AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(ConsumerBridge.class);
    private final Map<String, List<EventConsumer<?>>> eventHandlers = new ConcurrentHashMap<>();
    private final Gson mapper = new Gson();
    private KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    public ConsumerBridge(String bootstrapServer, String groupId, String[] topics) {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        setConsumer(props);
        this.consumer.subscribe(Arrays.asList(topics));

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        logger.info("Scanning for @EventHandler annotated consumers...");

        Reflections reflections = new Reflections(
                "com.dthvinh.libs.kafka.consumer"
        );

        Set<Class<?>> handlerClasses = reflections.getTypesAnnotatedWith(EventHandler.class);

        for (Class<?> clazz : handlerClasses) {
            if (!EventConsumer.class.isAssignableFrom(clazz)) {
                logger.info("Class {} is annotated with @EventHandler but does not implement EventConsumer", clazz.getName());
                continue;
            }

            EventHandler annotation = clazz.getAnnotation(EventHandler.class);
            String eventKey = annotation.eventKey();

            if (eventKey == null || eventKey.isBlank()) {
                logger.info("Invalid sender in @EventHandler on {}", clazz.getName());
                continue;
            }

            try {
                EventConsumer<?> handler = (EventConsumer<?>) clazz.getConstructor().newInstance();
                registerConsumer(eventKey, handler);
                logger.info("Registered handler {} for eventKey {}", clazz.getSimpleName(), eventKey);
            } catch (Exception e) {
                logger.error("Failed to instantiate event handler {}: {}",
                        clazz.getName(), e.getMessage());
            }
        }

        if (eventHandlers.isEmpty()) {
            logger.info("No valid @EventHandler annotated consumers found!");
        } else {
            logger.info("Found {} event handler(s)", eventHandlers.size());
        }
    }

    private void setConsumer(Properties props) {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        consumer = new KafkaConsumer<>(props);
        Thread.currentThread().setContextClassLoader(context);
    }

    @Override
    public void run() {
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record);
                }
            }
        } catch (Exception e) {
            logger.error("Error in Kafka consumer loop: {}", e.getMessage());
        } finally {
            consumer.close();
            logger.info("Muzic Kafka Consumer stopped");
        }
    }

    private void processRecord(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();

        if (value == null || value.isBlank()) {
            logger.info("Received empty/null value from topic");
            return;
        }

        Object data = mapper.fromJson(value, Object.class);

        List<EventConsumer<?>> handlers = eventHandlers.get(key);

        if (handlers == null || handlers.isEmpty()) {
            logger.info("No handler registered for eventKey: {}", key);
            return;
        }

        for (EventConsumer<?> rawHandler : handlers) {
            try {
                @SuppressWarnings("unchecked")
                EventConsumer<Object> handler = (EventConsumer<Object>) rawHandler;
                handler.handleData(data);
                logger.debug("Processed event {} with handler {}", key, rawHandler.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("Handler {} failed for event {}: {}",
                        rawHandler.getClass().getSimpleName(), key, e.getMessage(), e);
            }
        }
    }

    private void registerConsumer(String key, EventConsumer<?> consumer) {
        List<EventConsumer<?>> consumers = eventHandlers.get(key);
        if (consumers == null) {
            consumers = new ArrayList<>();
        }
        consumers.add(consumer);
        eventHandlers.put(key, consumers);
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void close() {
        shutdown();
    }
}