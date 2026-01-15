package com.dthvinh.libs.kafka;

import com.dthvinh.libs.kafka.base.EventConsumer;
import com.dthvinh.libs.kafka.common.ApplicationConstants;
import com.dthvinh.libs.kafka.common.Env;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(immediate = true)
public class Starter {

    private final Logger logger = LoggerFactory.getLogger(Starter.class);
    private final List<EventConsumer<?>> consumers = new CopyOnWriteArrayList<>();
    private ConsumerBridge bridge;

    @Reference(
            service = EventConsumer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC
    )
    protected void bindConsumer(EventConsumer<?> consumer) {
        consumers.add(consumer);
        if (bridge != null) {
            bridge.registerConsumerFromAnnotation(consumer);
        }
    }

    protected void unbindConsumer(EventConsumer<?> consumer) {
        consumers.remove(consumer);
        if (bridge != null) {
            bridge.unregisterConsumerInstance(consumer);
        }
    }

    @Activate
    public void start() {
        logger.info("Start service-b server");
        bridge = new ConsumerBridge(
                Env.KAFKA_BOOTSTRAP_SERVER,
                "person-service-group",
                new String[]{ApplicationConstants.AppGlobalTopic},
                consumers
        );
        new Thread(bridge, "Kafka-Consumer-Thread").start();
    }

    @Deactivate
    public void stop() {
        if (bridge != null) {
            bridge.close();
        }
    }
}