package com.dthvinh.libs.kafka;

import com.dthvinh.libs.kafka.common.ApplicationConstants;
import com.dthvinh.libs.kafka.common.Env;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class Starter {

    private final Logger logger = LoggerFactory.getLogger(Starter.class);
    private ConsumerBridge bridge;

    @Activate
    public void start() {
        logger.info("Start service-b server");
        bridge = new ConsumerBridge(
                Env.KAFKA_BOOTSTRAP_SERVER,
                "person-service-group",
                new String[]{ApplicationConstants.AppGlobalTopic}
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