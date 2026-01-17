package com.dthvinh.rs;

import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(service = Servlet.class, property = {
        "osgi.http.whiteboard.servlet.pattern=/api/health",
        "osgi.http.whiteboard.servlet.name=service-b-test-servlet",
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=default)"
})
public class HealthResource extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(200);
        resp.setContentType("application/json");
        resp.getWriter().write("""
                {
                    "status": "ok"
                }
                """);
    }
}
