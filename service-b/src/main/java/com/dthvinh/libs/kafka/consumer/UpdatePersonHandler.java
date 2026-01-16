package com.dthvinh.libs.kafka.consumer;

import com.dthvinh.constants.Events;
import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import com.dthvinh.libs.kafka.dto.UpdatePersonData;
import com.dthvinh.libs.redis.CachingService;
import com.dthvinh.libs.redis.KeyService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true, service = EventConsumer.class)
@EventHandler(eventKey = Events.UpdatePersonEvent)
public class UpdatePersonHandler extends EventConsumer<UpdatePersonData> {
    @Reference(
            service = CachingService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
    )
    private CachingService cachingService;

    @Override
    public void handleData(UpdatePersonData e) {
        log.info("Received update person request {}", e);

        cachingService.cache(KeyService.createKey(e.id()), e);
    }
}