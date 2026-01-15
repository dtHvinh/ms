package com.dthvinh.libs.kafka.consumer;

import com.dthvinh.constants.Events;
import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = EventConsumer.class)
@EventHandler(eventKey = Events.DeletePersonEvent)
public class DeletePersonHandler extends EventConsumer<String> {

    @Override
    public void handleData(String e) {
        log.info("Received delete person request {}", e);
    }
}