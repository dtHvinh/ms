package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JedisKeyValueStoreTest {

    @Test
    void constructorValidatesHost() {
        assertThrows(IllegalArgumentException.class, () -> new JedisKeyValueStore("", 6379));
    }

    @Test
    void methodsExecuteEvenIfRedisUnavailable() {
        // Use an unreachable port to avoid depending on a real Redis server.
        KeyValueStore store = new JedisKeyValueStore("127.0.0.1", 1);

        assertThrows(Exception.class, () -> store.get("k"));
        assertThrows(Exception.class, () -> store.set("k", "v"));
        assertThrows(Exception.class, () -> store.del("k"));

        assertDoesNotThrow(store::close);
    }
}
