package ru.bellintegrator.users_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import ru.bellintegrator.users_service.entity.UserEntity;
import ru.bellintegrator.users_service.mapper.UserMapper;
import ru.bellintegrator.users_service.model.UserDto;
import ru.bellintegrator.users_service.model.UserFilter;
import ru.bellintegrator.users_service.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private KafkaTemplate<String, UserDto> kafkaTemplate;

    private final UUID testId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private UserDto testUserDto;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setId(testId);
        testUserDto.setFirstname("Test");
        testUserDto.setLastname("User");

        testUserEntity = new UserEntity();
        testUserEntity.setId(testId);
        testUserEntity.setFirstname("Test");
        testUserEntity.setLastname("User");
    }

    @Test
    void getUserById_ShouldReturnUserDto_WhenFound() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUserEntity));
        when(userMapper.toDomainUser(testUserEntity)).thenReturn(testUserDto);

        UserDto result = userService.getUserById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());

        verify(userRepository, times(1)).findById(testId);
        verify(userMapper, times(1)).toDomainUser(testUserEntity);
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotFound() {
        when(userRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getUserById(testId));

        verify(userRepository, times(1)).findById(testId);
        verify(userMapper, never()).toDomainUser(any());
    }

    @Test
    void getAll_ShouldReturnPageOfUserDto() {
        UserFilter filter = new UserFilter("Test", null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        List<UserEntity> entityList = List.of(testUserEntity);
        Page<UserEntity> entityPage = new PageImpl<>(entityList, pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(entityPage);
        when(userMapper.toDomainUser(testUserEntity)).thenReturn(testUserDto);

        Page<UserDto> resultPage = userService.getAll(filter, pageable);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(testId, resultPage.getContent().get(0).getId());

        verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(userMapper, times(1)).toDomainUser(testUserEntity);
    }

    @Test
    void createUser_ShouldSendKafkaMessage_WhenValid() {
        UserDto userToCreate = new UserDto();
        userToCreate.setFirstname("New");
        userToCreate.setId(null);

        when(kafkaTemplate.send(anyString(), any(UserDto.class))).thenReturn(null);

        assertDoesNotThrow(() -> userService.createUser(userToCreate));

        verify(kafkaTemplate, times(1)).send(eq("USER_CREATE"), eq(userToCreate));
    }

    @Test
    void createUser_ShouldThrowException_WhenIdIsNotNull() {
        UserDto userToCreate = new UserDto();
        userToCreate.setId(testId);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userToCreate));

        verify(kafkaTemplate, never()).send(anyString(), any(UserDto.class));
    }

    @Test
    void updateUser_ShouldSendKafkaMessage_WhenFound() {
        when(userRepository.existsById(testId)).thenReturn(true);
        when(kafkaTemplate.send(anyString(), any(UserDto.class))).thenReturn(null);

        assertDoesNotThrow(() -> userService.updateUser(testUserDto));

        verify(userRepository, times(1)).existsById(testId);
        verify(kafkaTemplate, times(1)).send(eq("USER_UPDATE"), eq(testUserDto));
    }

    @Test
    void updateUser_ShouldThrowIllegalArgumentException_WhenIdIsNull() {
        UserDto userToUpdate = new UserDto();
        userToUpdate.setId(null);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userToUpdate));

        verify(userRepository, never()).existsById(any());
        verify(kafkaTemplate, never()).send(anyString(), any(UserDto.class));
    }

    @Test
    void updateUser_ShouldThrowNoSuchElementException_WhenNotFound() {
        when(userRepository.existsById(testId)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> userService.updateUser(testUserDto));

        verify(userRepository, times(1)).existsById(testId);
        verify(kafkaTemplate, never()).send(anyString(), any(UserDto.class));
    }

    @Test
    void deleteUserById_ShouldSendKafkaMessage_WhenFound() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUserEntity));
        when(userMapper.toDomainUser(testUserEntity)).thenReturn(testUserDto);
        when(kafkaTemplate.send(anyString(), any(UserDto.class))).thenReturn(null);

        assertDoesNotThrow(() -> userService.deleteUserById(testId));

        verify(userRepository, times(1)).findById(testId);
        verify(kafkaTemplate, times(1)).send(eq("USER_DELETE"), eq(testUserDto));
    }

    @Test
    void deleteUserById_ShouldThrowException_WhenNotFound() {
        when(userRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.deleteUserById(testId));

        verify(userRepository, times(1)).findById(testId);
        verify(kafkaTemplate, never()).send(anyString(), any(UserDto.class));
    }
}