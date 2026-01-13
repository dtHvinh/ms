package demo.ms.servicea.api;

import demo.ms.common.PersonEvent;

public interface PersonEventProducer extends AutoCloseable {
    void send(PersonEvent event);

    @Override
    void close();
}
