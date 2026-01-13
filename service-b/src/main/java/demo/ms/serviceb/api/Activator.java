package demo.ms.serviceb.api;

import java.util.function.Supplier;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class Activator implements BundleActivator {
    private final Supplier<? extends AutoCloseable> starter;
    private AutoCloseable service;

    public Activator() {
        this(ServiceB::startFromEnv);
    }

    Activator(Supplier<? extends AutoCloseable> starter) {
        this.starter = starter;
    }

    @Override
    public void start(BundleContext context) {
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
