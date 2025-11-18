package ru.bellintegrator;

import java.util.UUID;

//DTO используется для передачи данных через REST API
public record User (
        UUID id,
        String firstname,
        String lastname,
        int age
) {

}
