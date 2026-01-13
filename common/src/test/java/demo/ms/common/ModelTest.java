package demo.ms.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ModelTest {

    @Test
    void personGettersEqualsHashCode() {
        Person a1 = new Person("1", "Alice", 30);
        Person a2 = new Person("1", "Alice", 30);
        Person b = new Person("2", "Bob", 31);

        assertEquals("1", a1.getId());
        assertEquals("Alice", a1.getName());
        assertEquals(30, a1.getAge());

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, b);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "not-a-person");
    }

    @Test
    void personEventGettersEqualsHashCode() {
        Person p1 = new Person("1", "Alice", 30);
        PersonEvent e1 = new PersonEvent(PersonEvent.Operation.CREATE, p1);
        PersonEvent e2 = new PersonEvent(PersonEvent.Operation.CREATE, new Person("1", "Alice", 30));
        PersonEvent e3 = new PersonEvent(PersonEvent.Operation.DELETE, p1);

        assertEquals(PersonEvent.Operation.CREATE, e1.getOperation());
        assertEquals(p1, e1.getPerson());

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
        assertNotEquals(e1, null);
        assertNotEquals(e1, "not-an-event");
    }

    @Test
    void jsonMapperSingleton() {
        assertSame(Json.mapper(), Json.mapper());
    }
}
