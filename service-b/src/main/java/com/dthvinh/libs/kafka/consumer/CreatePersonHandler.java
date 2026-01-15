package com.dthvinh.libs.kafka.consumer;

import com.dthvinh.constants.Events;
import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import com.dthvinh.libs.kafka.dto.CreatePersonData;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = EventConsumer.class)
@EventHandler(eventKey = Events.CreatePersonEvent)
public class CreatePersonHandler extends EventConsumer<CreatePersonData> {

    @Override
    public void handleData(CreatePersonData e) {
        log.info("Received create person request {}", e);
    }
}
