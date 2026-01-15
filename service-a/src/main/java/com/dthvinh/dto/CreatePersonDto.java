package com.dthvinh.dto;

public record CreatePersonDto(String name, int age) {
    public String getName() {
        return name;
    }
}

