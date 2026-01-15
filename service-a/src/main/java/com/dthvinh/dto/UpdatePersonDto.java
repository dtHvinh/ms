package com.dthvinh.dto;

public record UpdatePersonDto(int id, String name, int age) {
    public int getId() {
        return id;
    }
}
