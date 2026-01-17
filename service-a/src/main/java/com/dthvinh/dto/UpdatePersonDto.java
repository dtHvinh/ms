package com.dthvinh.dto;

import java.util.UUID;

public record UpdatePersonDto(UUID id, String name, int age) {
    public UUID getId() {
        return id;
    }
}
