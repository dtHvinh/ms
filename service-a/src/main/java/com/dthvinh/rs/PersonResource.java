package com.dthvinh.rs;

import com.dthvinh.dto.CreatePersonDto;
import com.dthvinh.dto.UpdatePersonDto;
import com.dthvinh.libs.common.ER;
import com.dthvinh.libs.kafka.event.CreatePersonEventArgs;
import com.dthvinh.libs.kafka.event.DeletePersonEventArgs;
import com.dthvinh.libs.kafka.event.UpdatePersonEventArgs;
import com.dthvinh.libs.kafka.publisher.KafkaPublisher;
import com.dthvinh.libs.servlet.Endpoint;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Map;

@Component(service = Servlet.class, property = {
        "osgi.http.whiteboard.servlet.pattern=/api/person/*",
        "osgi.http.whiteboard.servlet.name=persons-servlet",
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=default)"
})
public class PersonResource extends Endpoint {
    @Override
    protected void handleGet() throws IOException {
        try {
            sendOk(Map.of(
                    "message", "GET /api/person - List all persons (not implemented yet)",
                    "status", "ok"
            ));
        } catch (Exception ex) {
            log.error("GET failed", ex);
            send(500, "{\"reason\":\"Internal server error during GET\"}");
        }
    }

    @Override
    protected void handlePost() throws IOException {
        CreatePersonDto dto;
        try {
            dto = readJsonBody(CreatePersonDto.class);
        } catch (Exception ex) {
            log.warn("Invalid POST body", ex);
            sendBadRequest(Map.of("reason", ER.REQUEST_BODY_UNEXPECTED_EMPTY));
            return;
        }

        KafkaPublisher publisher = getOrCreatePublisher();
        if (publisher == null) {
            log.warn("Kafka publisher not available - env var missing?");
            sendBadRequest(Map.of("reason", ER.KAFKA_SERVER_ENV_NOT_SET));
            return;
        }

        try {
            CreatePersonEventArgs e = new CreatePersonEventArgs(dto);
            publisher.send(e);
            log.info("Person creation event queued: {}", dto);
            sendObject(201, Map.of(
                    "status", "request queued",
                    "name", dto.getName()
            ));
        } catch (Exception ex) {
            log.error("Failed to queue person creation event", ex);
            send(500, "{\"reason\":\"Kafka send failed\"}");
        }
    }

    @Override
    protected void handlePut() throws IOException {
        UpdatePersonDto dto;
        try {
            dto = readJsonBody(UpdatePersonDto.class);
        } catch (Exception ex) {
            log.warn("Invalid PUT body", ex);
            sendBadRequest(Map.of("reason", "Invalid or empty request body"));
            return;
        }

        KafkaPublisher publisher = getOrCreatePublisher();
        if (publisher == null) {
            sendBadRequest(Map.of("reason", ER.KAFKA_SERVER_ENV_NOT_SET));
            return;
        }

        try {
            UpdatePersonEventArgs e = new UpdatePersonEventArgs(dto);
            publisher.send(e);
            log.info("Person update event queued for id: {}", dto.getId());
            sendObject(200, Map.of(
                    "status", "update request queued",
                    "personId", dto.getId()
            ));
        } catch (Exception ex) {
            log.error("Failed to queue person update event for id: {}", dto.getId(), ex);
            send(500, "{\"reason\":\"Kafka send failed\"}");
        }
    }

    @Override
    protected void handleDelete() throws IOException {
        String personId = query("id");

        if (personId == null || personId.trim().isEmpty()) {
            sendBadRequest(Map.of("reason", "Missing person ID"));
            return;
        }

        KafkaPublisher publisher = getOrCreatePublisher();
        if (publisher == null) {
            sendBadRequest(Map.of("reason", ER.KAFKA_SERVER_ENV_NOT_SET));
            return;
        }

        try {
            DeletePersonEventArgs e = new DeletePersonEventArgs(personId);
            publisher.send(e);
            log.info("Person delete event queued for id: {}", personId);
            sendObject(200, Map.of(
                    "status", "delete request queued",
                    "personId", personId
            ));
        } catch (Exception ex) {
            log.error("Failed to queue person delete event for id: {}", personId, ex);
            send(500, "{\"reason\":\"Kafka send failed\"}");
        }
    }
}
