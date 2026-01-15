package com.dthvinh.dto;

public record CreatePersonDto(int id, String name, int age) {
    public String getName() {
        return name;
    }
}

