package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ServiceBFromEnvTest {
    @AfterEach
    void cleanup() {
        System.clearProperty("KAFKA_BOOTSTRAP_SERVERS");
        System.clearProperty("KAFKA_TOPIC");
        System.clearProperty("KAFKA_GROUP_ID");
        System.clearProperty("REDIS_HOST");
        System.clearProperty("REDIS_PORT");
    }

    @Test
    void startFromEnvWithoutStartingConsumer() {
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        System.setProperty("KAFKA_TOPIC", "person-events");
        System.setProperty("KAFKA_GROUP_ID", "service-b-test");

        // Point Redis to an unreachable port so no real dependency is required.
        System.setProperty("REDIS_HOST", "127.0.0.1");
        System.setProperty("REDIS_PORT", "1");

        ServiceB service = ServiceB.startFromEnv(false);
        assertDoesNotThrow(service::close);
    }
}
