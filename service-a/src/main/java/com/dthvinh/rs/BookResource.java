package com.dthvinh.rs;

import java.io.IOException;

import javax.servlet.Servlet;

import org.osgi.service.component.annotations.Component;

import com.dthvinh.libs.servlet.Endpoint;

@Component(service = Servlet.class, property = {
        "osgi.http.whiteboard.servlet.pattern=/api/books/*",
        "osgi.http.whiteboard.servlet.name=books-servlet",
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=default)"
})
public class BookResource extends Endpoint {
    @Override
    protected void handleGet() throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().write("book id = " + 12);
    }
}
