package com.dthvinh.libs.servlet;

import com.dthvinh.libs.common.ApplicationConstants;
import com.dthvinh.libs.common.Env;
import com.dthvinh.libs.kafka.publisher.KafkaPublisher;
import com.google.gson.Gson;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class Endpoint extends HttpServlet {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    public HttpServletRequest req;
    public HttpServletResponse resp;
    protected Gson gson = new Gson();
    protected volatile KafkaPublisher publisher;

    @Deactivate
    void deactivate() {
        KafkaPublisher toClose = publisher;
        publisher = null;
        if (toClose != null) {
            try {
                toClose.close();
            } catch (Exception ex) {
                log.warn("Failed to close KafkaPublisher", ex);
            }
        }
    }

    protected KafkaPublisher getOrCreatePublisher() {
        KafkaPublisher existing = publisher;
        if (existing != null) {
            return existing;
        }

        synchronized (this) {
            existing = publisher;
            if (existing != null) {
                return existing;
            }

            String bootstrapServers = Env.KAFKA_BOOTSTRAP_SERVER;
            if (bootstrapServers == null || bootstrapServers.isBlank()) {
                return null;
            }

            publisher = new KafkaPublisher(bootstrapServers, ApplicationConstants.AppGlobalTopic);
            return publisher;
        }
    }

    @Override
    protected final void service(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        long startNs = System.nanoTime();
        String method = req.getMethod();
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        String pathWithQuery = query == null ? uri : (uri + "?" + query);

        this.req = req;
        this.resp = resp;

        try {
            log.info("=> {} {}", method, pathWithQuery);
            super.service(req, resp);
        } catch (Exception ex) {
            log.error("!! {} {}", method, pathWithQuery, ex);
            throw ex;
        } finally {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            int status = resp.getStatus();
            log.info("<= {} {} -> {} ({}ms)", method, pathWithQuery, status, elapsedMs);
            this.req = null;
            this.resp = null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handleGet();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handlePost();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handlePut();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handleDelete();
    }

    protected void handleGet() throws IOException {
        sendMethodNotAllowed();
    }

    protected void handlePost() throws IOException {
        sendMethodNotAllowed();
    }

    protected void handlePut() throws IOException {
        sendMethodNotAllowed();
    }

    protected void handleDelete() throws IOException {
        sendMethodNotAllowed();
    }

    protected String query(String name) {
        return req.getParameter(name);
    }

    protected Optional<String> queryOpt(String name) {
        return Optional.ofNullable(req.getParameter(name));
    }

    protected String path() {
        return req.getRequestURI();
    }

    protected String method() {
        return req.getMethod();
    }

    protected void sendOk(String body) throws IOException {
        send(HttpServletResponse.SC_OK, body);
    }

    protected void sendOk(Object body) throws IOException {
        sendObject(HttpServletResponse.SC_OK, body);
    }

    protected void sendCreated(String body) throws IOException {
        send(HttpServletResponse.SC_CREATED, body);
    }

    protected void sendNotFound() throws IOException {
        send(HttpServletResponse.SC_NOT_FOUND, "");
    }

    protected void sendBadRequest(String body) throws IOException {
        send(HttpServletResponse.SC_BAD_REQUEST, body);
    }

    protected void sendBadRequest(Object body) throws IOException {
        sendObject(HttpServletResponse.SC_BAD_REQUEST, body);
    }

    protected void sendMethodNotAllowed() {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected void send(int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write(body);
    }

    protected void sendObject(int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (body == null) {
            return;
        }

        String json = gson.toJson(body);

        PrintWriter writer = resp.getWriter();
        writer.write(json);
        writer.flush();
    }

    protected <T> T readJsonBody(Class<T> clazz) throws IOException {
        StringBuilder json = new StringBuilder();
        String line;

        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }

        Gson gson = new Gson();
        return gson.fromJson(json.toString(), clazz);
    }
}
