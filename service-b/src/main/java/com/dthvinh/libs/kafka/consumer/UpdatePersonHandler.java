package com.dthvinh.libs.kafka.consumer;

import com.dthvinh.constants.Events;
import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import com.dthvinh.libs.kafka.dto.UpdatePersonData;

@EventHandler(eventKey = Events.UpdatePersonEvent)
public class UpdatePersonHandler extends EventConsumer<UpdatePersonData> {

    @Override
    public void handleData(UpdatePersonData e) {
        log.info("Received update person request {}", e);
    }
}