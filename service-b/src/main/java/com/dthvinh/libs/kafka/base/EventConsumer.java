package com.dthvinh.libs.kafka.base;

import jdk.jshell.spi.ExecutionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventConsumer<TData> {
    protected Logger log = LoggerFactory.getLogger(EventConsumer.class);

    public void handleData(TData e) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("Bro forgot to override this");
    }
}
