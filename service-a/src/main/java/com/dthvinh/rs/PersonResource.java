package com.dthvinh.rs;

import com.dthvinh.dto.CreatePersonDto;
import com.dthvinh.libs.common.ApplicationConstants;
import com.dthvinh.libs.common.ER;
import com.dthvinh.libs.common.Env;
import com.dthvinh.libs.kafka.event.CreatePersonEventArgs;
import com.dthvinh.libs.kafka.publisher.KafkaPublisher;
import com.dthvinh.libs.servlet.Endpoint;
import org.osgi.service.component.annotations.Deactivate;
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
    private volatile KafkaPublisher publisher;

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

    private KafkaPublisher getOrCreatePublisher() {
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
    protected void handlePost() throws IOException {
        CreatePersonDto dto;
        try {
            dto = readJsonBody(CreatePersonDto.class);

            log.debug("Data object: {}", dto);
        } catch (Exception ex) {
            sendBadRequest(Map.of("reason", ER.REQUEST_BODY_UNEXPECTED_EMPTY));
            return;
        }

        KafkaPublisher publisher = getOrCreatePublisher();
        if (publisher == null) {
            sendBadRequest(Map.of("reason", "KAFKA_BOOTSTRAP_SERVER is not set"));
            return;
        }

        try {
            CreatePersonEventArgs e = new CreatePersonEventArgs(dto);
            publisher.send(e);
            sendObject(201, Map.of("status", "queued"));
        } catch (Exception ex) {
            log.error("Kafka send failed", ex);
            send(500, "{\"reason\":\"Kafka send failed\"}");
        }
    }
}
