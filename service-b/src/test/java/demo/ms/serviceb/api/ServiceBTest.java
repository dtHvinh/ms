package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.junit.jupiter.api.Test;

class ServiceBTest {
    @Test
    void closeShutsDownConsumerAndRepository() {
        RedisPersonRepository repo = new RedisPersonRepository(new RedisPersonRepositoryTest.InMemoryStore());
        PersonEventHandler handler = new PersonEventHandler(repo);
        MockConsumer<String, String> mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        KafkaPersonEventConsumer consumer = new KafkaPersonEventConsumer(mockConsumer, "t", handler);

        ServiceB service = new ServiceB(repo, consumer);

        assertDoesNotThrow(service::close);
    }
}
