package com.dthvinh.libs.kafka.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventConsumer<TData> {
    protected Logger log = LoggerFactory.getLogger(EventConsumer.class);

    public void handleData(TData e) {
        throw new UnsupportedOperationException("EventConsumer.handleData must be overridden");
    }
}
