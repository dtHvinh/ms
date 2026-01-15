package com.dthvinh.libs.kafka;

import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerBridge implements Runnable, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(ConsumerBridge.class);
    private final Map<String, List<EventConsumer<?>>> eventHandlers = new ConcurrentHashMap<>();
    private final Gson mapper = new Gson();
    private KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    public ConsumerBridge(String bootstrapServer, String groupId, String[] topics, Collection<EventConsumer<?>> initialHandlers) {
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

        registerEventHandlers(initialHandlers);
    }

    private void registerEventHandlers(Collection<EventConsumer<?>> initialHandlers) {
        int available = initialHandlers == null ? 0 : initialHandlers.size();
        logger.info("Registering EventConsumer service(s) (count={})", available);

        int registeredCount = 0;

        if (initialHandlers != null) {
            for (EventConsumer<?> handler : initialHandlers) {
                if (registerConsumerFromAnnotation(handler)) {
                    registeredCount++;
                }
            }
        }

        if (registeredCount == 0) {
            logger.warn("No event handlers were registered. In OSGi, make sure handlers are DS services of type EventConsumer.");
        } else {
            logger.info("Successfully registered {} event handler(s)", registeredCount);
        }
    }

    public boolean registerConsumerFromAnnotation(EventConsumer<?> handler) {
        if (handler == null) {
            return false;
        }

        Class<?> clazz = handler.getClass();
        EventHandler annotation = clazz.getAnnotation(EventHandler.class);
        if (annotation == null) {
            logger.warn("EventConsumer {} is missing @EventHandler annotation", clazz.getName());
            return false;
        }

        String eventKey = annotation.eventKey();
        if (eventKey == null || eventKey.trim().isEmpty()) {
            logger.warn("Invalid/missing eventKey in @EventHandler on {}", clazz.getName());
            return false;
        }

        registerConsumer(eventKey, handler);
        logger.info("Registered handler {} for eventKey '{}'", clazz.getSimpleName(), eventKey);
        return true;
    }

    public void unregisterConsumerInstance(EventConsumer<?> handler) {
        if (handler == null) {
            return;
        }

        for (Map.Entry<String, List<EventConsumer<?>>> entry : eventHandlers.entrySet()) {
            List<EventConsumer<?>> handlers = entry.getValue();
            if (handlers != null) {
                handlers.removeIf(existing -> existing == handler);
            }
        }
    }

    private void setConsumer(Properties props) {
        // This pattern is usually fine — helps avoid classloader visibility issues with Kafka libs
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            consumer = new KafkaConsumer<>(props);
        } finally {
            Thread.currentThread().setContextClassLoader(context);
        }
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
            logger.error("Error in Kafka consumer loop: {}", e, e);
        } finally {
            if (consumer != null) {
                try {
                    consumer.close();
                } catch (Exception e) {
                    logger.warn("Error closing consumer", e);
                }
            }
            logger.info("Kafka Consumer stopped");
        }
    }

    private void processRecord(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();

        if (value == null || value.trim().isEmpty()) {
            logger.debug("Received empty/null value from topic → skipping");
            return;
        }

        Object data;
        try {
            data = mapper.fromJson(value, Object.class);
        } catch (Exception e) {
            logger.warn("Failed to parse JSON from record (key={}, topic={}, partition={}, offset={}): {}",
                    key, record.topic(), record.partition(), record.offset(), e.toString());
            return;
        }

        List<EventConsumer<?>> handlers = eventHandlers.get(key);

        if (handlers == null || handlers.isEmpty()) {
            logger.debug("No handler registered for eventKey: {}", key);
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
                        rawHandler.getClass().getSimpleName(), key, e, e);
            }
        }
    }

    private void registerConsumer(String key, EventConsumer<?> consumer) {
        eventHandlers.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void close() {
        shutdown();
    }
}