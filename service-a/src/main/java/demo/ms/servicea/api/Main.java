package demo.ms.servicea.api;

public final class Main {
    public static void main(String[] args) throws Exception {
        ServiceA service = ServiceA.startFromEnv();
        Runtime.getRuntime().addShutdownHook(new Thread(service::close));

        if (args != null && args.length > 0 && "--exit-immediately".equals(args[0])) {
            service.close();
            return;
        }
        Thread.currentThread().join();
    }
}
