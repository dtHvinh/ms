package com.dthvinh.libs.servlet;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import com.dthvinh.libs.kafka.publisher.KafkaPublisher;

class EndpointLifecycleTest {

    static class TestEndpoint extends Endpoint {
        @Override
        protected void handleGet() {
            // no-op
        }
    }

    @Test
    void getOrCreatePublisher_returnsExistingInstance() {
        TestEndpoint endpoint = new TestEndpoint();
        KafkaPublisher publisher = mock(KafkaPublisher.class);
        endpoint.publisher = publisher;

        assertSame(publisher, endpoint.getOrCreatePublisher());
        assertSame(publisher, endpoint.getOrCreatePublisher());
    }

    @Test
    void deactivate_closesPublisher_andNullsField() {
        TestEndpoint endpoint = new TestEndpoint();
        KafkaPublisher publisher = mock(KafkaPublisher.class);
        endpoint.publisher = publisher;

        endpoint.deactivate();

        assertNull(endpoint.publisher);
        verify(publisher).close();
    }

    @Test
    void sendMethodNotAllowed_sets405() {
        TestEndpoint endpoint = new TestEndpoint();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        endpoint.resp = resp;

        endpoint.sendMethodNotAllowed();

        verify(resp).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
