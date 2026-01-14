package com.dthvinh.libs.kafka.event;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventArgs<TData> {
    public String event;
    public TData data;
}

