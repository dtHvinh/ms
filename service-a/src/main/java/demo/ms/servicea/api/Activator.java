package demo.ms.servicea.api;

import java.util.function.Supplier;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class Activator implements BundleActivator {
    private final Supplier<? extends AutoCloseable> starter;
    private AutoCloseable service;

    public Activator() {
        this(() -> {
            try {
                return ServiceA.startFromEnv();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    Activator(Supplier<? extends AutoCloseable> starter) {
        this.starter = starter;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        service = starter.get();
    }

    @Override
    public void stop(BundleContext context) {
        if (service != null) {
            try {
                service.close();
            } catch (Exception ignored) {
                // best-effort shutdown
            }
        }
    }
}
