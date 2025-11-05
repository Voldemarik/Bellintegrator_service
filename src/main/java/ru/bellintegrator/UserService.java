package ru.bellintegrator;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

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

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    };

    public User getUserById(
            UUID id
    ) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found user by id = " + id
                ));

        return toDomainUser(userEntity);
    }

    public List<User> getAllUsers() {

        List<UserEntity> allEntities = userRepository.findAll();

        return allEntities.stream()
                .map(this::toDomainUser)
                .toList();
    }

    public User createUser(
            User userToCreate
    ) {
        if (userToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }
        var newUserEntity = userRepository.save(
                new UserEntity(
                null,
                userToCreate.firstname(),
                userToCreate.lastname(),
                userToCreate.age()
        ));

        return toDomainUser(newUserEntity);
    }

    public User updateUserById(
            UUID id,
            User userToUpdate
    ) {

        var userEntity = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found user by id = " + id));

        var updatedUserEntity = userRepository.save(new UserEntity(
                userEntity.getId(),
                userToUpdate.firstname(),
                userToUpdate.lastname(),
                userToUpdate.age()
        ));

        return toDomainUser(updatedUserEntity);
    }

    public void deleteUserById(
            UUID id
    ) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Not found user by id = " + id);
        }

        userRepository.deleteById(id);
    }

    private User toDomainUser(
            UserEntity userEntity
    ) {
        return new User(
                userEntity.getId(),
                userEntity.getFirstname(),
                userEntity.getLastname(),
                userEntity.getAge()
        );
    }
}
