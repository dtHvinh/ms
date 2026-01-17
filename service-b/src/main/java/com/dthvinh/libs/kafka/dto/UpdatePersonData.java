package com.dthvinh.libs.kafka.dto;

import java.util.UUID;

public record UpdatePersonData(UUID id, String name, int age) {
}
