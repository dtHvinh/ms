package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import demo.ms.common.Person;
import demo.ms.common.PersonEvent;

class KafkaPersonEventConsumerValidationTest {
    @Test
    void validatesConstructorArgs() {
        RedisPersonRepository repo = new RedisPersonRepository(new RedisPersonRepositoryTest.InMemoryStore());
        PersonEventHandler handler = new PersonEventHandler(repo);

        assertThrows(IllegalArgumentException.class, () -> new KafkaPersonEventConsumer("", "g", "t", handler));
        assertThrows(IllegalArgumentException.class, () -> new KafkaPersonEventConsumer("b:9092", "", "t", handler));
        assertThrows(IllegalArgumentException.class, () -> new KafkaPersonEventConsumer("b:9092", "g", "", handler));
        assertThrows(IllegalArgumentException.class, () -> new KafkaPersonEventConsumer("b:9092", "g", "t", null));

        // sanity: handler itself can accept repo
        handler.handle(new PersonEvent(PersonEvent.Operation.CREATE, new Person("1", "A", 1)));
    }
}
