package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ActivatorTest {
    @Test
    void startStopUsesInjectedStarter() {
        CloseCounter counter = new CloseCounter();
        Activator activator = new Activator(() -> (AutoCloseable) () -> counter.closed = true);

        activator.start(null);
        activator.stop(null);
        assertTrue(counter.closed);
    }

    static final class CloseCounter {
        volatile boolean closed;
    }
}
