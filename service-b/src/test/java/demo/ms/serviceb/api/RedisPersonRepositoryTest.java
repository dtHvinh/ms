package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.Test;

import demo.ms.common.Person;

class RedisPersonRepositoryTest {
    @Test
    void keyFormat() {
        assertEquals("person:1", RedisPersonRepository.key("1"));
    }

    @Test
    void upsertAndGetRoundTrip() {
        InMemoryStore store = new InMemoryStore();
        RedisPersonRepository repo = new RedisPersonRepository(store);
        repo.upsert(new Person("1", "A", 1));
        Optional<Person> got = repo.get("1");

        assertTrue(got.isPresent());
        assertEquals("1", got.get().getId());
    }

    @Test
    void deleteCallsDel() {
        InMemoryStore store = new InMemoryStore();
        RedisPersonRepository repo = new RedisPersonRepository(store);
        repo.upsert(new Person("1", "A", 1));
        repo.delete("1");
        assertTrue(repo.get("1").isEmpty());
    }

    static final class InMemoryStore implements KeyValueStore {
        private final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

        @Override
        public String get(String key) {
            return map.get(key);
        }

        @Override
        public void set(String key, String value) {
            map.put(key, value);
        }

        @Override
        public void del(String key) {
            map.remove(key);
        }

        @Override
        public void close() {
        }
    }
}
