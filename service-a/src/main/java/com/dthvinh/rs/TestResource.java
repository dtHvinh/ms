package com.dthvinh.rs;

import java.io.IOException;
import javax.servlet.Servlet;

import com.dthvinh.libs.servlet.Endpoint;
import org.osgi.service.component.annotations.Component;

@Component(
        service = Servlet.class,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/api/test",
                "osgi.http.whiteboard.servlet.name=test-servlet",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=default)"
        }
)
public class TestResource extends Endpoint {

    @Override
    protected void handleGet() throws IOException {
        sendOk("Hello from Endpoint");
    }
}
