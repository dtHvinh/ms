package demo.ms.servicea.api;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public final class ServiceA implements AutoCloseable {
    private final HttpServer server;
    private final PersonEventProducer producer;

    public ServiceA(HttpServer server, PersonEventProducer producer) {
        this.server = server;
        this.producer = producer;
    }

    public static ServiceA start(int port, PersonStore store, PersonEventProducer producer) throws Exception {
        if (store == null) {
            throw new IllegalArgumentException("store is required");
        }
        if (producer == null) {
            throw new IllegalArgumentException("producer is required");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            if ("/health".equals(exchange.getRequestURI().getPath())) {
                HttpUtil.sendText(exchange, 200, "OK");
                return;
            }
            HttpUtil.sendText(exchange, 404, "Not found");
        });
        server.createContext("/persons", new PersonHttpHandler(store, producer));
        server.createContext("/persons/", new PersonHttpHandler(store, producer));
        server.setExecutor(null);
        server.start();

        return new ServiceA(server, producer);
    }

    public static ServiceA startFromEnv() throws Exception {
        int port = Env.getInt("HTTP_PORT", 8080);
        String bootstrap = Env.get("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
        String topic = Env.get("KAFKA_TOPIC", "person-events");

        PersonStore store = new PersonStore();
        KafkaPersonEventProducer producer = new KafkaPersonEventProducer(bootstrap, topic);
        return start(port, store, producer);
    }

    @Override
    public void close() {
        try {
            producer.close();
        } finally {
            server.stop(0);
        }
    }
}
