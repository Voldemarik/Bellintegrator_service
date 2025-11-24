package ru.bellintegrator.users_service.model;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
//DTO используется для передачи данных через REST API
public class UserDto implements Serializable {
    private UUID id;
    private String firstname;
    private String lastname;
    private Integer age;
}
