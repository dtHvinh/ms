package demo.ms.servicea.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import demo.ms.common.Json;
import demo.ms.common.Person;
import demo.ms.common.PersonEvent;

import java.io.IOException;
import java.net.URI;

public final class PersonHttpHandler implements HttpHandler {
    private final PersonStore store;
    private final PersonEventProducer producer;

    public PersonHttpHandler(PersonStore store, PersonEventProducer producer) {
        this.store = store;
        this.producer = producer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();

            // Routes:
            // POST /persons
            // GET /persons/{id}
            // PUT /persons/{id}
            // DELETE /persons/{id}

            if (path.equals("/persons") && exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                handleCreate(exchange);
                return;
            }

            if (path.startsWith("/persons/")) {
                String id = path.substring("/persons/".length());
                String method = exchange.getRequestMethod().toUpperCase();
                switch (method) {
                    case "GET" -> handleGet(exchange, id);
                    case "PUT" -> handleUpdate(exchange, id);
                    case "DELETE" -> handleDelete(exchange, id);
                    default -> HttpUtil.sendText(exchange, 405, "Method not allowed");
                }
                return;
            }

            HttpUtil.sendText(exchange, 404, "Not found");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendText(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendText(exchange, 500, "Internal error");
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        Person person = Json.fromJson(HttpUtil.readBody(exchange), Person.class);
        Person created = store.create(person);
        producer.send(new PersonEvent(PersonEvent.Operation.CREATE, created));
        HttpUtil.sendJson(exchange, 201, Json.toJson(created));
    }

    private void handleGet(HttpExchange exchange, String id) throws IOException {
        Person person = store.get(id).orElse(null);
        if (person == null) {
            HttpUtil.sendText(exchange, 404, "Not found");
            return;
        }
        producer.send(new PersonEvent(PersonEvent.Operation.GET, person));
        HttpUtil.sendJson(exchange, 200, Json.toJson(person));
    }

    private void handleUpdate(HttpExchange exchange, String id) throws IOException {
        Person body = Json.fromJson(HttpUtil.readBody(exchange), Person.class);
        Person updated = store.update(id, body);
        producer.send(new PersonEvent(PersonEvent.Operation.UPDATE, updated));
        HttpUtil.sendJson(exchange, 200, Json.toJson(updated));
    }

    private void handleDelete(HttpExchange exchange, String id) throws IOException {
        store.delete(id);
        producer.send(new PersonEvent(PersonEvent.Operation.DELETE, new Person(id, "DELETED", 0)));
        HttpUtil.sendText(exchange, 204, "");
    }
}
