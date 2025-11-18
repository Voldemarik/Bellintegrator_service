package ru.bellintegrator;

import java.util.UUID;

//DTO передает данные о событиях в Kafka
public record UserEvent(
        UUID id,
        String operation,
        User user,
        long timestamp
) {
    public UserEvent(
            String operation,
            User user
    ) {
        this(user.id(), operation, user, System.currentTimeMillis());
    }
}