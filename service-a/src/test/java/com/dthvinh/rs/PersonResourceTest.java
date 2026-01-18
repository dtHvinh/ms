package com.dthvinh.rs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.dthvinh.libs.common.ER;
import com.dthvinh.libs.kafka.event.CreatePersonEventArgs;
import com.dthvinh.libs.kafka.event.DeletePersonEventArgs;
import com.dthvinh.libs.kafka.event.UpdatePersonEventArgs;
import com.dthvinh.libs.kafka.publisher.KafkaPublisher;

class PersonResourceTest {

    static class TestablePersonResource extends PersonResource {
        KafkaPublisher publisherToReturn;

        @Override
        protected KafkaPublisher getOrCreatePublisher() {
            return publisherToReturn;
        }
    }

    @Test
    void post_invalidBody_returns400() throws Exception {
        TestablePersonResource resource = new TestablePersonResource();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        when(req.getReader()).thenThrow(new IOException("boom"));

        resource.req = req;
        resource.resp = resp;

        resource.handlePost();

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String body = out.toString();
        assertTrue(body.contains(ER.REQUEST_BODY_UNEXPECTED_EMPTY));
    }

    @Test
    void post_missingPublisher_returns400() throws Exception {
        TestablePersonResource resource = new TestablePersonResource();
        resource.publisherToReturn = null;

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"A\",\"age\":10}")));

        resource.req = req;
        resource.resp = resp;

        resource.handlePost();

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains(ER.KAFKA_SERVER_ENV_NOT_SET));
    }

    @Test
    void post_success_sendsKafkaEvent_andReturns201() throws Exception {
        TestablePersonResource resource = new TestablePersonResource();
        KafkaPublisher publisher = mock(KafkaPublisher.class);
        resource.publisherToReturn = publisher;

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"John\",\"age\":31}")));

        resource.req = req;
        resource.resp = resp;

        resource.handlePost();

        verify(resp).setStatus(201);

        ArgumentCaptor<CreatePersonEventArgs> captor = ArgumentCaptor.forClass(CreatePersonEventArgs.class);
        verify(publisher).send(captor.capture());
        assertEquals("CreatePersonEvent", captor.getValue().event);
        assertEquals("John", captor.getValue().data.getName());

        String body = out.toString();
        assertTrue(body.contains("request queued"));
        assertTrue(body.contains("John"));
    }

    @Test
    void put_invalidBody_returns400() throws Exception {
        TestablePersonResource resource = new TestablePersonResource();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        when(req.getReader()).thenThrow(new IOException("boom"));

        resource.req = req;
        resource.resp = resp;

        resource.handlePut();

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains("Invalid"));
    }

    @Test
    void put_success_sendsKafkaEvent_andReturns200() throws Exception {
        TestablePersonResource resource = new TestablePersonResource();
        KafkaPublisher publisher = mock(KafkaPublisher.class);
        resource.publisherToReturn = publisher;

        UUID id = UUID.randomUUID();
        String json = "{\"id\":\"" + id + "\",\"name\":\"Jane\",\"age\":22}";

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        resource.req = req;
        resource.resp = resp;

        resource.handlePut();

        verify(resp).setStatus(HttpServletResponse.SC_OK);

        ArgumentCaptor<UpdatePersonEventArgs> captor = ArgumentCaptor.forClass(UpdatePersonEventArgs.class);
        verify(publisher).send(captor.capture());
        assertEquals("UpdatePersonEvent", captor.getValue().event);
        assertEquals(id, captor.getValue().data.getId());
        assertTrue(out.toString().contains(id.toString()));
    }

    @Test
    void delete_missingId_returns400() throws Exception {
        TestablePersonResource resource = new TestablePersonResource();
        resource.publisherToReturn = mock(KafkaPublisher.class);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        when(req.getParameter("id")).thenReturn(" ");

        resource.req = req;
        resource.resp = resp;

        resource.handleDelete();

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains("Missing person ID"));
        verify(resource.publisherToReturn, never()).send(any());
    }

    @Test
    void delete_success_sendsKafkaEvent_andReturns200() throws Exception {
        TestablePersonResource resource = new TestablePersonResource();
        KafkaPublisher publisher = mock(KafkaPublisher.class);
        resource.publisherToReturn = publisher;

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        when(req.getParameter("id")).thenReturn("abc");

        resource.req = req;
        resource.resp = resp;

        resource.handleDelete();

        verify(resp).setStatus(HttpServletResponse.SC_OK);

        ArgumentCaptor<DeletePersonEventArgs> captor = ArgumentCaptor.forClass(DeletePersonEventArgs.class);
        verify(publisher).send(captor.capture());
        assertEquals("DeletePersonEvent", captor.getValue().event);
        assertEquals("abc", captor.getValue().data);

        assertTrue(out.toString().contains("delete request queued"));
        assertTrue(out.toString().contains("abc"));
    }
}
