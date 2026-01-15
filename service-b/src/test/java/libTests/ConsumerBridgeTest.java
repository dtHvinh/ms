package libTests;

import com.dthvinh.constants.Events;
import com.dthvinh.libs.kafka.annotation.EventHandler;
import com.dthvinh.libs.kafka.base.EventConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerBridgeTest {
    private final Map<String, List<EventConsumer<?>>> eventHandlers = new ConcurrentHashMap<>();

    @Test
    void findConsumers() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections(
                "com.dthvinh.libs.kafka.consumer"
        );

        Set<Class<?>> handlerClasses = reflections.getTypesAnnotatedWith(EventHandler.class);

        Assertions.assertEquals(3, handlerClasses.size());

        for (Class<?> clazz : handlerClasses) {
            if (!EventConsumer.class.isAssignableFrom(clazz)) {
                continue;
            }

            EventHandler annotation = clazz.getAnnotation(EventHandler.class);
            String eventKey = annotation.eventKey();

            EventConsumer<?> handler = (EventConsumer<?>) clazz.getConstructor().newInstance();
            registerConsumer(eventKey, handler);
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
