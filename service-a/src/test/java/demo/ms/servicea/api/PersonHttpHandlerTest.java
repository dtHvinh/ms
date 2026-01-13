package demo.ms.servicea.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import demo.ms.common.Json;
import demo.ms.common.Person;
import demo.ms.common.PersonEvent;

class PersonHttpHandlerTest {

    @Test
    void createThenGetProducesEvents() throws Exception {
        PersonStore store = new PersonStore();
        CapturingProducer producer = new CapturingProducer();
        PersonHttpHandler handler = new PersonHttpHandler(store, producer);

        // POST /persons
        Person p = new Person("1", "Alice", 30);
        FakeExchange post = FakeExchange.request("POST", "/persons", Json.toJson(p));
        handler.handle(post);
        assertEquals(201, post.status);
        assertEquals(PersonEvent.Operation.CREATE, producer.last().getOperation());

        // GET /persons/1
        FakeExchange get = FakeExchange.request("GET", "/persons/1", null);
        handler.handle(get);
        assertEquals(200, get.status);
        assertEquals(PersonEvent.Operation.GET, producer.last().getOperation());
        Person body = Json.fromJson(get.bodyAsString(), Person.class);
        assertEquals("1", body.getId());
    }

    @Test
    void updateAndDelete() throws Exception {
        PersonStore store = new PersonStore();
        CapturingProducer producer = new CapturingProducer();
        PersonHttpHandler handler = new PersonHttpHandler(store, producer);

        handler.handle(FakeExchange.request("POST", "/persons", Json.toJson(new Person("1", "A", 1))));

        FakeExchange put = FakeExchange.request("PUT", "/persons/1", Json.toJson(new Person("x", "B", 2)));
        handler.handle(put);
        assertEquals(200, put.status);
        assertEquals(PersonEvent.Operation.UPDATE, producer.last().getOperation());

        FakeExchange del = FakeExchange.request("DELETE", "/persons/1", null);
        handler.handle(del);
        assertEquals(204, del.status);
        assertEquals(PersonEvent.Operation.DELETE, producer.last().getOperation());
    }

    @Test
    void unknownPathReturns404() throws Exception {
        PersonStore store = new PersonStore();
        CapturingProducer producer = new CapturingProducer();
        PersonHttpHandler handler = new PersonHttpHandler(store, producer);

        FakeExchange ex = FakeExchange.request("GET", "/nope", null);
        handler.handle(ex);
        assertEquals(404, ex.status);
    }

    static final class CapturingProducer implements PersonEventProducer {
        private PersonEvent last;

        @Override
        public void send(PersonEvent event) {
            this.last = event;
        }

        PersonEvent last() {
            return last;
        }

        @Override
        public void close() {
        }
    }

    static final class FakeExchange extends HttpExchange {
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final String method;
        private final URI uri;
        private final ByteArrayInputStream requestBody;
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

        int status;

        static FakeExchange request(String method, String path, String body) {
            byte[] bytes = body == null ? new byte[0] : body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return new FakeExchange(method, URI.create(path), new ByteArrayInputStream(bytes));
        }

        FakeExchange(String method, URI uri, ByteArrayInputStream requestBody) {
            this.method = method;
            this.uri = uri;
            this.requestBody = requestBody;
        }

        String bodyAsString() {
            return responseBody.toString(java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return uri;
        }

        @Override
        public String getRequestMethod() {
            return method;
        }

        @Override
        public HttpContext getHttpContext() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public InputStream getRequestBody() {
            return requestBody;
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
            this.status = rCode;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public int getResponseCode() {
            return status;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }
    }
}
