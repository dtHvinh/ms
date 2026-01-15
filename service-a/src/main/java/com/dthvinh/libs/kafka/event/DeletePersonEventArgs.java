package com.dthvinh.libs.kafka.event;

public class DeletePersonEventArgs extends EventArgs<String> {
    public DeletePersonEventArgs(String personId) {
        super("DeletePersonEvent", personId);
    }
}
