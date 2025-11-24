package ru.bellintegrator.users_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "firstname")
    private String firstname;
    @Column(name = "lastname")
    private String lastname;
    @Column(name = "age")
    private int age;
}
