package libTests;

import com.dthvinh.constants.Events;
import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import com.dthvinh.libs.kafka.consumer.CreatePersonHandler;
import com.dthvinh.libs.kafka.consumer.DeletePersonHandler;
import com.dthvinh.libs.kafka.consumer.UpdatePersonHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerBridgeTest {
    private final Map<String, List<EventConsumer<?>>> eventHandlers = new ConcurrentHashMap<>();

    @Test
    void findConsumers() {
        List<EventConsumer<?>> consumers = List.of(
                new CreatePersonHandler(),
                new DeletePersonHandler(),
                new UpdatePersonHandler()
        );

        for (EventConsumer<?> consumer : consumers) {
            EventHandler annotation = consumer.getClass().getAnnotation(EventHandler.class);
            Assertions.assertNotNull(annotation);
            registerConsumer(annotation.eventKey(), consumer);
        }

        Assertions.assertFalse(eventHandlers.get(Events.CreatePersonEvent).isEmpty());
        Assertions.assertFalse(eventHandlers.get(Events.DeletePersonEvent).isEmpty());
        Assertions.assertFalse(eventHandlers.get(Events.UpdatePersonEvent).isEmpty());
    }

    private void registerConsumer(String key, EventConsumer<?> consumer) {
        List<EventConsumer<?>> consumers = eventHandlers.get(key);
        if (consumers == null) {
            consumers = new ArrayList<>();
        }
        consumers.add(consumer);
        eventHandlers.put(key, consumers);
    }
}
