package demo.ms.servicea.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import demo.ms.common.Person;

class PersonStoreTest {
    @Test
    void createGetUpdateDelete() {
        PersonStore store = new PersonStore();
        Person p = new Person("1", "Alice", 30);
        store.create(p);

        assertEquals(p, store.get("1").orElseThrow());

        Person updated = store.update("1", new Person("ignored", "Alice2", 31));
        assertEquals("1", updated.getId());
        assertEquals("Alice2", updated.getName());
        assertEquals(31, updated.getAge());

        store.delete("1");
        assertTrue(store.get("1").isEmpty());
    }

    @Test
    void createDuplicateThrows() {
        PersonStore store = new PersonStore();
        store.create(new Person("1", "A", 1));
        assertThrows(IllegalArgumentException.class, () -> store.create(new Person("1", "A", 1)));
    }
}
