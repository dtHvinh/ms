package com.dthvinh.rs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

class BookResourceTest {

    @Test
    void get_writesPlainText() throws Exception {
        BookResource resource = new BookResource();

        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        resource.resp = resp;

        resource.handleGet();

        verify(resp).setContentType("text/plain");
        assertTrue(out.toString().contains("book id"));
    }
}
