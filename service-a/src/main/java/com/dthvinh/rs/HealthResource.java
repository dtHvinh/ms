package com.dthvinh.rs;

import com.dthvinh.libs.servlet.Endpoint;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Map;

@Component(service = Servlet.class, property = {
        "osgi.http.whiteboard.servlet.pattern=/api/health",
        "osgi.http.whiteboard.servlet.name=test-servlet",
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=default)"
})
public class HealthResource extends Endpoint {

    @Override
    protected void handleGet() throws IOException {
        sendOk(Map.of("status", "ok"));
    }
}
