package demo.ms.servicea.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EnvTest {
    @AfterEach
    void cleanup() {
        System.clearProperty("X_TEST");
        System.clearProperty("X_INT");
    }

    @Test
    void getUsesDefaultWhenMissing() {
        assertEquals("d", Env.get("__MISSING__", "d"));
    }

    @Test
    void getUsesSystemPropertyFallback() {
        System.setProperty("X_TEST", "v");
        assertEquals("v", Env.get("X_TEST", "d"));
    }

    @Test
    void getIntParsesAndFallsBackOnInvalid() {
        System.setProperty("X_INT", "123");
        assertEquals(123, Env.getInt("X_INT", 9));

        System.setProperty("X_INT", "not-an-int");
        assertEquals(9, Env.getInt("X_INT", 9));
    }
}
