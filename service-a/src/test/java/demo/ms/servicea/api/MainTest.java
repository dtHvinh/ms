package demo.ms.servicea.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MainTest {
    @AfterEach
    void cleanup() {
        System.clearProperty("HTTP_PORT");
        System.clearProperty("KAFKA_BOOTSTRAP_SERVERS");
        System.clearProperty("KAFKA_TOPIC");
    }

    @Test
    void mainCanExitImmediately() throws Exception {
        System.setProperty("HTTP_PORT", "0");
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        System.setProperty("KAFKA_TOPIC", "person-events");

        assertDoesNotThrow(() -> Main.main(new String[] { "--exit-immediately" }));
    }
}
