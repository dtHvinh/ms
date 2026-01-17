package com.dthvinh.libs.kafka.consumer;

import com.dthvinh.constants.Events;
import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import com.dthvinh.libs.kafka.dto.CreatePersonData;
import com.dthvinh.libs.redis.CachingService;
import com.dthvinh.libs.redis.KeyService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.UUID;

@Component(immediate = true, service = EventConsumer.class)
@EventHandler(eventKey = Events.CreatePersonEvent)
public class CreatePersonHandler extends EventConsumer<CreatePersonData> {
    @Reference(
            service = CachingService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
    )
    private CachingService cachingService;

    @Override
    public void handleData(CreatePersonData e) {
        log.info("Received create person request {}", e);

        cachingService.cache(KeyService.createKey(UUID.randomUUID()), e);
    }
}
