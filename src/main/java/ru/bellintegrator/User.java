package ru.bellintegrator;

import java.util.UUID;

public record User (
        UUID id,
        String firstname,
        String lastname,
        int age
) {

}
