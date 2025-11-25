package ru.bellintegrator.users_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bellintegrator.users_service.entity.UserEntity;
import ru.bellintegrator.users_service.mapper.UserMapper;
import ru.bellintegrator.users_service.model.UserDto;
import ru.bellintegrator.users_service.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventListenerTest {

    @InjectMocks
    private EventListener eventListener;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private final UUID testId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setId(testId);
        testUserDto.setFirstname("Test");
        testUserDto.setLastname("User");
        testUserDto.setAge(30);

        UserEntity testUserEntity = new UserEntity();
        testUserEntity.setId(testId);
        testUserEntity.setFirstname("Test");
        testUserEntity.setLastname("User");
        testUserEntity.setAge(30);
    }

    @Test
    void handleCreate_ShouldSaveUser_WhenIdIsNull() {
        UserDto userToCreate = new UserDto();
        userToCreate.setFirstname("New");
        userToCreate.setId(null);

        UserEntity newEntity = new UserEntity();
        newEntity.setId(testId);

        when(userMapper.toUserEntity(userToCreate)).thenReturn(newEntity);
        when(userRepository.save(newEntity)).thenReturn(newEntity);

        eventListener.handleCreate(userToCreate);

        verify(userMapper, times(1)).toUserEntity(userToCreate);
        verify(userRepository, times(1)).save(newEntity);
    }

    @Test
    void handleCreate_ShouldSkipProcessing_WhenIdIsNotNull() {
        UserDto userToCreate = new UserDto();

        assertThrows(NullPointerException.class, () -> eventListener.handleCreate(userToCreate));

        verify(userRepository, never()).save(any(UserEntity.class));
    }


    @Test
    void handleUpdate_ShouldUpdateOnlyFirstName_WhenFoundAndPartialDto() {
        UserDto partialUpdateDto = new UserDto();
        partialUpdateDto.setId(testId);
        partialUpdateDto.setFirstname("UpdatedName");
        partialUpdateDto.setLastname(null);
        partialUpdateDto.setAge(null);

        UserEntity existingEntity = new UserEntity();
        existingEntity.setId(testId);
        existingEntity.setFirstname("OldName");
        existingEntity.setLastname("OldSurname");
        existingEntity.setAge(25);

        when(userRepository.findById(testId)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingEntity);

        eventListener.handleUpdate(partialUpdateDto);

        verify(userRepository, times(1)).findById(testId);
        verify(userRepository, times(1)).save(existingEntity);
    }

    @Test
    void handleUpdate_ShouldSkipProcessing_WhenIdIsNull() {
        UserDto userToUpdate = new UserDto();

        eventListener.handleUpdate(userToUpdate);

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void handleUpdate_ShouldSkipProcessing_WhenUserNotFound() {
        when(userRepository.findById(testId)).thenReturn(Optional.empty());

        eventListener.handleUpdate(testUserDto);

        verify(userRepository, times(1)).findById(testId);
        verify(userRepository, never()).save(any(UserEntity.class));
    }


    @Test
    void handleDelete_ShouldDeleteUser_WhenFound() {
        eventListener.handleDelete(testUserDto);

        verify(userRepository, times(1)).deleteById(testId);
    }

    @Test
    void handleDelete_ShouldSkipProcessing_WhenIdIsNull() {
        UserDto userToDelete = new UserDto();
        userToDelete.setId(null);

        eventListener.handleDelete(userToDelete);

        verify(userRepository, never()).deleteById(any());
    }
}