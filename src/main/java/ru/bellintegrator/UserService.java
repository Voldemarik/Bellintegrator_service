package ru.bellintegrator;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

//    private final Map<UUID, User> userMap = Map.of(
//            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
//            new User(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), "Иван", "Иванов", 25),
//
//            UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
//            new User(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), "Мария", "Петрова", 30),
//
//            UUID.fromString("123e4567-e89b-12d3-a456-426614174002"),
//            new User(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"), "Алексей", "Сидоров", 28)
//    );

    private final Map<UUID, User>  userMap;

    public UserService() {
        userMap = new HashMap<>();
    };


    public User getUserById(
            UUID id
    ) {
        if (!userMap.containsKey(id)) {
            throw new NoSuchElementException("Not found user by id = " + id);
        }
        return userMap.get(id);
    }

    public List<User> getAllUsers() {
        return userMap.values().stream().toList();
    }

    public User createUser(
            User userToCreate
    ) {
        if (userToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }
        var newUser = new User(
                UUID.randomUUID(),
                userToCreate.firstname(),
                userToCreate.lastname(),
                userToCreate.age()
        );
        userMap.put(newUser.id(), newUser);
        return newUser;
    }

    public User updateUserById(
            UUID id,
            User userToUpdate
    ) {
        if (!userMap.containsKey(id)) {
            throw new NoSuchElementException("Not found user by id = " + id);
        }
        var user = userMap.get(id);
        var updatedUser = new User(
                user.id(),
                userToUpdate.firstname(),
                userToUpdate.lastname(),
                userToUpdate.age()
        );
        userMap.put(user.id(), updatedUser);
        return updatedUser;
    }

    public void deleteUserById(
            UUID id
    ) {
        if (!userMap.containsKey(id)) {
            throw new NoSuchElementException("Not found user by id = " + id);
        }
        userMap.remove(id);
    }
}
