package demo.ms.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JsonTest {
    @Test
    void roundTripPerson() {
        Person person = new Person("1", "Alice", 30);
        String json = Json.toJson(person);
        Person parsed = Json.fromJson(json, Person.class);
        assertEquals(person, parsed);
    }

    @Test
    void fromJsonInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> Json.fromJson("{bad}", Person.class));
    }

    @Test
    void toJsonNullStillWorks() {
        String json = Json.toJson(null);
        assertEquals("null", json);
    }
}
