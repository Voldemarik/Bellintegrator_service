package ru.bellintegrator.users_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.bellintegrator.users_service.entity.UserEntity;
import ru.bellintegrator.users_service.mapper.UserMapper;
import ru.bellintegrator.users_service.model.UserDto;
import ru.bellintegrator.users_service.repository.UserRepository;

import java.util.NoSuchElementException;

@Service
public class EventListener {
    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public EventListener(UserMapper userMapper, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @KafkaListener(topics = "USER_CREATE", groupId = "users-service")
    public void handleCreate(UserDto userToCreate) {
        try {
            if (userToCreate.getId() != null) {
                log.warn("Received CREATE event with ID: {}. Skipping.", userToCreate.getId());
                return;
            }
            UserEntity newEntity = userMapper.toUserEntity(userToCreate);
            userRepository.save(newEntity);
            log.info("User created successfully with ID: {}", newEntity.getId());
        } catch (Exception e) {
            log.error("Error processing CREATE event for user: {}", userToCreate, e);
            throw e;
        }
    }

    @KafkaListener(topics = "USER_UPDATE", groupId = "users-service")
    public void handleUpdate(UserDto userToUpdate) {
        try {
            if (userToUpdate.getId() == null) {
                log.error("Received UPDATE event without ID. Skipping.");
                return;
            }
            UserEntity existingEntity = userRepository.findById(userToUpdate.getId())
                    .orElseThrow(() -> new NoSuchElementException("User not found for update via Kafka: " + userToUpdate.getId()));
            updateEntity(userToUpdate, existingEntity);
            userRepository.save(existingEntity);
            log.info("User updated successfully with ID: {}", existingEntity.getId());
        } catch (NoSuchElementException e) {
            log.warn("User ID {} not found for update (Possible race condition/late event). Skipping.", userToUpdate.getId());
        } catch (Exception e) {
            log.error("Error processing UPDATE event for user ID: {}", userToUpdate.getId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "USER_DELETE", groupId = "users-service")
    public void handleDelete(UserDto userToDelete) {
        try {
            if (userToDelete.getId() == null) {
                log.error("Received DELETE event without ID. Skipping.");
                return;
            }
            userRepository.deleteById(userToDelete.getId());
            log.info("User deleted successfully with ID: {}", userToDelete.getId());
        } catch (Exception e) {
            log.error("Error processing DELETE event for user ID: {}", userToDelete.getId(), e);
            throw e;
        }
    }

    private void updateEntity(UserDto userDto, UserEntity entity) {
        if (userDto.getFirstname() != null) {
            entity.setFirstname(userDto.getFirstname());
        }
        if (userDto.getLastname() != null) {
            entity.setLastname(userDto.getLastname());
        }
        if (userDto.getAge() != null) {
            entity.setAge(userDto.getAge());
        }
    }
}
