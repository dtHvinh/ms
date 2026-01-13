package demo.ms.servicea.api;

import demo.ms.common.Json;
import demo.ms.common.PersonEvent;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public final class KafkaPersonEventProducer implements PersonEventProducer {
    private final Producer<String, String> producer;
    private final String topic;

    public KafkaPersonEventProducer(String bootstrapServers, String topic) {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            throw new IllegalArgumentException("bootstrapServers is required");
        }
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic is required");
        }
        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "all");

        this.producer = new KafkaProducer<>(props);
    }

    KafkaPersonEventProducer(Producer<String, String> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public void send(PersonEvent event) {
        if (event == null) {
            return;
        }
        String key = event.getPerson() != null ? event.getPerson().getId() : null;
        String payload = Json.toJson(event);
        producer.send(new ProducerRecord<>(topic, key, payload));
    }

    @Override
    public void close() {
        producer.close();
    }
}
