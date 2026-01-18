package com.dthvinh.rs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

class HealthResourceTest {

    @Test
    void get_returnsOkJson() throws Exception {
        HealthResource resource = new HealthResource();

        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        resource.resp = resp;

        resource.handleGet();

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        assertTrue(out.toString().contains("\"ok\""));
    }
}
