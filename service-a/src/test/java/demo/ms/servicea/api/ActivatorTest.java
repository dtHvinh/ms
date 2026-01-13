package demo.ms.servicea.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ActivatorTest {
    @Test
    void startStopUsesInjectedStarter() throws Exception {
        CloseCounter closeCounter = new CloseCounter();
        Activator activator = new Activator(() -> {
            // Return any ServiceA instance; we only need to ensure close() is called.
            // Use a lightweight wrapper via ServiceA.start with port 0 and a no-op
            // producer.
            try {
                return ServiceA.start(0, new PersonStore(), new NoOpProducer(closeCounter));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        activator.start(null);
        activator.stop(null);

        assertTrue(closeCounter.closed);
    }

    static final class CloseCounter {
        volatile boolean closed;
    }

    static final class NoOpProducer implements PersonEventProducer {
        private final CloseCounter counter;

        NoOpProducer(CloseCounter counter) {
            this.counter = counter;
        }

        @Override
        public void send(demo.ms.common.PersonEvent event) {
        }

        @Override
        public void close() {
            counter.closed = true;
        }
    }
}
