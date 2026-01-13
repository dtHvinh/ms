package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import demo.ms.common.Person;

class RedisPersonRepositoryValidationTest {
    @Test
    void validatesConstructorAndArgs() {
        assertThrows(IllegalArgumentException.class, () -> new RedisPersonRepository((KeyValueStore) null));

        RedisPersonRepository repo = new RedisPersonRepository(new RedisPersonRepositoryTest.InMemoryStore());

        assertThrows(IllegalArgumentException.class, () -> repo.upsert(null));
        assertThrows(IllegalArgumentException.class, () -> repo.upsert(new Person("", "A", 1)));
        assertThrows(IllegalArgumentException.class, () -> repo.delete(""));

        assertTrue(repo.get("").isEmpty());
    }
}
