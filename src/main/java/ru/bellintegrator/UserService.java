package ru.bellintegrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private static final String USER_EVENTS = "user-events";

    private static final String USERS_CACHE = "users";
    private static final String USER_BY_ID_CACHE = "user";

    public UserService(UserRepository userRepository, ObjectMapper objectMapper, UserMapper userMapper, KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Cacheable(
            value = USER_BY_ID_CACHE,
            key = "#id"
    )
    public User getUserById(
            UUID id
    ) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Not found user by id = " + id
                ));

        return toDomainUser(userEntity);
    }

    @Cacheable(
            value = USERS_CACHE,
            key = "'all'"
    )
    public List<User> getAllUsers() {

        List<UserEntity> allEntities = userRepository.findAll();

        return allEntities.stream()
                .map(this::toDomainUser)
                .toList();
    }

    public Page<User> getAllUsers(
            Pageable pageable
    ) {
        Page<UserEntity> userEntityPage = userRepository.findAll(pageable);

        List<User> userPage = userEntityPage.getContent().stream()
                .map(this::toDomainUser)
                .toList();

        return new PageImpl<>(userPage, pageable, userEntityPage.getTotalElements());
    }

    public Page<User> getAllUsersWithFilters(
            UserFilter userFilter
    ) {
        Pageable pageable = PageRequest.of(userFilter.getPage(), userFilter.getSize());

        Page<UserEntity> userEntityPageWithFilters = userRepository.findByFilters(
                userFilter.getFirstname(),
                userFilter.getLastname(),
                userFilter.getMinAge(),
                userFilter.getMaxAge(),
                pageable
        );

        List<User> userPageWithFilters = userEntityPageWithFilters.getContent().stream()
                .map(this::toDomainUser)
                .toList();

        return new PageImpl<>(userPageWithFilters, pageable, userEntityPageWithFilters.getTotalElements());
    }

    @Caching(
            evict = {
                    @CacheEvict(value = USERS_CACHE, key = "'all'"),
                    @CacheEvict(value = USER_BY_ID_CACHE, key = "#result.id()")
            }
    )
    @Transactional
    public User createUser(
            User userToCreate
    ) {
        if (userToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }

//        UserEntity newUserEntity = userRepository.save(
//                new UserEntity(
//                null,
//                userToCreate.firstname(),
//                userToCreate.lastname(),
//                userToCreate.age()
//        ));

        UserEntity newUserEntity = userMapper.toUserEntity(userToCreate);
        newUserEntity.setId(null);

        UserEntity savedEntity = userRepository.save(newUserEntity);
        User savedUser = toDomainUser(savedEntity);

        UserEvent event = new UserEvent("CREATE", savedUser);
        kafkaTemplate.send(USER_EVENTS, savedUser.id().toString(), event);

        return savedUser;
    }

    @Caching(
            evict = {
                    @CacheEvict(value = USERS_CACHE, key = "'all'"),
                    @CacheEvict(value = USER_BY_ID_CACHE, key = "#id")
            }
    )
    @Transactional
    public User updateUserById(
            UUID id,
            User userToUpdate
    ) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("Not found user by id = " + id);
        }

        UserEntity updatedUserEntity = userMapper.toUserEntity(userToUpdate);
        updatedUserEntity.setId(id);

        UserEntity savedEntity = userRepository.save(updatedUserEntity);
        User updatedUser = toDomainUser(savedEntity);

        UserEvent event = new UserEvent("UPDATE", updatedUser);
        kafkaTemplate.send(USER_EVENTS, updatedUser.id().toString(), event);

        System.out.println();

        return updatedUser;
    }

    @Caching(
            evict = {
                    @CacheEvict(value = USERS_CACHE, key = "'all'"),
                    @CacheEvict(value = USER_BY_ID_CACHE, key = "#id")
            }
    )
    @Transactional
    public void deleteUserById(
            UUID id
    ) {
//        if (!userRepository.existsById(id)) {
//            throw new NoSuchElementException("Not found user by id = " + id);
//        }

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Not found user by id = " + id
                ));

//        User userToDelete = getUserById(id);

        User userToDelete = toDomainUser(userEntity);
        userRepository.deleteById(id);

        UserEvent event = new UserEvent("DELETE", userToDelete);
        kafkaTemplate.send(USER_EVENTS, userToDelete.id().toString(), event);
    }

    private User toDomainUser(
            UserEntity userEntity
    ) {
//        return new User(
//                userEntity.getId(),
//                userEntity.getFirstname(),
//                userEntity.getLastname(),
//                userEntity.getAge()
//        );

        return objectMapper.convertValue(userEntity, User.class);
    }
}