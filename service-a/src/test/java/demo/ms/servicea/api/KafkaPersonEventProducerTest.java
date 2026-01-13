package demo.ms.servicea.api;

import demo.ms.common.Person;
import demo.ms.common.PersonEvent;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KafkaPersonEventProducerTest {
    @Test
    void sendNullDoesNothing() {
        MockProducer<String, String> mockProducer = new MockProducer<>(true, new StringSerializer(),
                new StringSerializer());
        KafkaPersonEventProducer producer = new KafkaPersonEventProducer(mockProducer, "t");

        producer.send(null);
        assertEquals(0, mockProducer.history().size());
    }

    @Test
    void sendEventCallsKafka() {
        MockProducer<String, String> mockProducer = new MockProducer<>(true, new StringSerializer(),
                new StringSerializer());
        KafkaPersonEventProducer producer = new KafkaPersonEventProducer(mockProducer, "t");

        producer.send(new PersonEvent(PersonEvent.Operation.CREATE, new Person("1", "A", 1)));
        assertEquals(1, mockProducer.history().size());
        assertEquals("t", mockProducer.history().get(0).topic());
        assertEquals("1", mockProducer.history().get(0).key());
    }
}
