package ru.bellintegrator.users_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.bellintegrator.users_service.model.UserDto;
import ru.bellintegrator.users_service.entity.UserEntity;
import ru.bellintegrator.users_service.model.UserFilter;
import ru.bellintegrator.users_service.repository.UserSpecification;
import ru.bellintegrator.users_service.repository.UserRepository;
import ru.bellintegrator.users_service.mapper.UserMapper;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, UserDto> kafkaTemplate;

    public UserService(UserRepository userRepository, UserMapper userMapper, KafkaTemplate<String, UserDto> kafkaTemplate) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Cacheable(value = "user", key = "#id")
    public UserDto getUserById(UUID id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Not found user by id = " + id)
        );
        return userMapper.toDomainUser(userEntity);
    }

    @Cacheable(value = "users", key = "{#f?.firstname, #f?.lastname, #f?.minAge, #f?.maxAge, #f?.page, #f?.size}")
    public Page<UserDto> getAll(UserFilter f) {
        log.info("Fetching user page for filter: {}", f);

        Specification<UserEntity> spec = UserSpecification.build(f);
        Pageable pageable = PageRequest.of(f.getPage(), f.getSize());
        Page<UserEntity> userEntityPage = userRepository.findAll(spec, pageable);

        List<UserDto> userListDTO = userEntityPage.stream()
                .map(userMapper::toDomainUser)
                .toList();

        return new PageImpl<>(userListDTO, pageable, userEntityPage.getTotalElements());
    }

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    public void createUser(UserDto userToCreate) {
        if (userToCreate.getId() != null) throw new IllegalArgumentException("ID must be null for creation");
        kafkaTemplate.send("USER_CREATE", userToCreate);
        log.info("Send CREATE event for user: {}", userToCreate);
    }

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    public void updateUser(UserDto userToUpdate) {
        if (userToUpdate.getId() == null) throw new IllegalArgumentException("ID must be not null for update");
        if (!userRepository.existsById(userToUpdate.getId()))
            throw new NoSuchElementException("Not found user by id = " + userToUpdate.getId());
        kafkaTemplate.send("USER_UPDATE", userToUpdate);
        log.info("Send UPDATE event for user ID: {}", userToUpdate.getId());
    }

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    public void deleteUserById(UUID id) {
       UserEntity entityToDelete = userRepository.findById(id)
               .orElseThrow(() -> new NoSuchElementException("Not found user by id = " + id));

        kafkaTemplate.send("USER_DELETE", userMapper.toDomainUser(entityToDelete));
        log.info("Send DELETE event for user ID: {}", userMapper.toDomainUser(entityToDelete).getId());
    }
}