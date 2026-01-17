package com.dthvinh.libs.kafka.publisher;

import com.dthvinh.libs.common.Env;
import com.dthvinh.libs.kafka.event.EventArgs;
import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaPublisher implements AutoCloseable {
    private final String topic;
    private final Gson mapper;
    private KafkaProducer<String, String> producer;

    public KafkaPublisher(String bootstrapServers, String topic) {
        this.topic = topic;
        this.mapper = new Gson();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        setProducer(props);
    }

    public KafkaPublisher(String topic) {
        this(Env.KAFKA_BOOTSTRAP_SERVER, topic);
    }

    private void setProducer(Properties props) {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        producer = new KafkaProducer<>(props);
        Thread.currentThread().setContextClassLoader(context);
    }

    public <T> void send(EventArgs<T> e) {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, e.event, mapper.toJson(e.data));

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Kafka send failed");
                exception.printStackTrace();
            } else {
                System.out.printf(
                        "Sent to topic=%s partition=%d offset=%d%n",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset()
                );
            }
        });
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}

