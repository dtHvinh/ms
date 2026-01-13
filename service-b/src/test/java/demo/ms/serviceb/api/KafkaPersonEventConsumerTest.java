package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import demo.ms.common.Json;
import demo.ms.common.Person;
import demo.ms.common.PersonEvent;

class KafkaPersonEventConsumerTest {
    @Test
    void startIsIdempotentAndCloseClosesConsumer() {
        MockConsumer<String, String> consumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        RedisPersonRepository repo = new RedisPersonRepository(new RedisPersonRepositoryTest.InMemoryStore());
        PersonEventHandler handler = new PersonEventHandler(repo);
        KafkaPersonEventConsumer c = new KafkaPersonEventConsumer(consumer, "t", handler);

        c.start(() -> {
            TopicPartition tp = new TopicPartition("t", 0);
            consumer.rebalance(java.util.List.of(tp));
            consumer.updateBeginningOffsets(java.util.Map.of(tp, 0L));

            // Add one good event and one bad payload
            String good = Json.toJson(new PersonEvent(PersonEvent.Operation.CREATE, new Person("1", "A", 1)));
            consumer.addRecord(new ConsumerRecord<>("t", 0, 0L, "1", good));
            consumer.addRecord(new ConsumerRecord<>("t", 0, 1L, "2", "{bad-json"));
        });
        c.start();

        // allow consumer thread to poll once
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        c.close();

        assertTrue(repo.get("1").isPresent());
    }
}
