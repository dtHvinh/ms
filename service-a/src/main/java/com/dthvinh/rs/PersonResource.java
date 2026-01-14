package com.dthvinh.rs;

import com.dthvinh.dto.CreatePersonDto;
import com.dthvinh.libs.common.ApplicationConstants;
import com.dthvinh.libs.common.ER;
import com.dthvinh.libs.common.Env;
import com.dthvinh.libs.kafka.event.CreatePersonEventArgs;
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
    private final KafkaPublisher publisher = new KafkaPublisher(Env.KAFKA_BOOTSTRAP_SERVER, ApplicationConstants.AppGlobalTopic);

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

        CreatePersonEventArgs e = new CreatePersonEventArgs(dto);
        publisher.send(e);
    }
}
