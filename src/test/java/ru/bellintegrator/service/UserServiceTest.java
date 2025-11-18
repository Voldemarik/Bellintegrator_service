package ru.bellintegrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.bellintegrator.UserService;
import ru.bellintegrator.User;
import ru.bellintegrator.UserEvent;
import ru.bellintegrator.UserFilter;
import ru.bellintegrator.UserEntity;
import ru.bellintegrator.UserMapper;
import ru.bellintegrator.UserRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private UUID testId;
    private User testUser;
    private UserEntity testUserEntity;
    private static final String TEST_USER_EVENT = "user-events";

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                objectMapper,
                userMapper,
                kafkaTemplate
        );

        testId = UUID.randomUUID();

        testUser = new User(
                testId,
                "Екатерина",
                "Сидорова",
                30
        );

        testUserEntity = new UserEntity(
                testId,
                "Екатерина",
                "Сидорова",
                30
        );
    }

    @Test
    void getUserById_Exist_ReturnUser() {
        when(userRepository.findById(testId))
                .thenReturn(Optional.of(testUserEntity));
        when(objectMapper.convertValue(testUserEntity, User.class))
                .thenReturn(testUser);

        User result = userService.getUserById(testId);

        assertNotNull(result);
        assertEquals(testUser, result);

        verify(userRepository).findById(testId);
        verify(objectMapper).convertValue(testUserEntity, User.class);
    }

    @Test
    void getUserById_NotExist_ThrowException() {
        when(userRepository.findById(testId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.getUserById(testId));

        verify(userRepository).findById(testId);
    }

    @Test
    void getAllUsers_ReturnList() {
        List<UserEntity> testListEntities = List.of(testUserEntity);

        when(userRepository.findAll())
                .thenReturn(testListEntities);
        when(objectMapper.convertValue(testUserEntity, User.class))
                .thenReturn(testUser);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));

        verify(userRepository).findAll();
        verify(objectMapper).convertValue(testUserEntity, User.class);
    }

    @Test
    void getAllUsers_ReturnEmptyList() {
        when(userRepository.findAll())
                .thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ReturnPage() {
        Pageable testPageable = PageRequest.of(0, 5);
        Page<UserEntity> testPageEntities = new PageImpl<>(
                List.of(testUserEntity),
                testPageable,
                1L
        );

        when(userRepository.findAll(testPageable))
                .thenReturn(testPageEntities);
        when(objectMapper.convertValue(testUserEntity, User.class))
                .thenReturn(testUser);

        Page<User> result = userService.getAllUsers(testPageable);

        assertNotNull(result);
        // Проверка контента
        assertEquals(1, result.getContent().size());
        assertEquals(testUser, result.getContent().get(0));
        // Проверка пагинации
        assertEquals(0, result.getNumber());
        assertEquals(5, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        verify(userRepository).findAll(testPageable);
        verify(objectMapper).convertValue(testUserEntity, User.class);
    }

    @Test
    void getAllUsers_ReturnEmptyPage() {
        Pageable testPageable = PageRequest.of(0, 5);
        Page<UserEntity> testEmptyPage = new PageImpl<>(
                List.of(),
                testPageable,
                0L
        );

        when(userRepository.findAll(testPageable))
                .thenReturn(testEmptyPage);

        Page<User> result = userService.getAllUsers(testPageable);

        assertNotNull(result);
        // Проверка контента
        assertTrue(result.getContent().isEmpty());
        // Проверка пагинации
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(userRepository).findAll(testPageable);
    }

    @Test
    void getAllUsersWithFilters_ReturnFilteredUsers() {
        UserFilter testUserFilter = new UserFilter(
                "Екатер",
                "Сидор",
                20,
                30,
                0,
                5
        );
        Pageable testPageable = PageRequest.of(
                testUserFilter.getPage(),
                testUserFilter.getSize()
        );
        Page<UserEntity> testFilteredPage = new PageImpl<>(
                List.of(testUserEntity),
                testPageable,
                1L
        );

        when(userRepository.findByFilters
                (testUserFilter.getFirstname(),
                   testUserFilter.getLastname(),
                   testUserFilter.getMinAge(),
                   testUserFilter.getMaxAge(),
                   testPageable)
        ).thenReturn(testFilteredPage);
        when(objectMapper.convertValue(testUserEntity, User.class))
                .thenReturn(testUser);

        Page<User> result = userService.getAllUsersWithFilters(testUserFilter);

        assertNotNull(result);
        // Проверка контента
        assertEquals(1, result.getContent().size());
        assertEquals(testUser, result.getContent().get(0));
        // Проверка пагинации
        assertEquals(0, result.getNumber());
        assertEquals(5, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        verify(userRepository).findByFilters(
                testUserFilter.getFirstname(),
                testUserFilter.getLastname(),
                testUserFilter.getMinAge(),
                testUserFilter.getMaxAge(),
                testPageable
        );
        verify(objectMapper).convertValue(testUserEntity, User.class);
    }

    @Test
    void getAllUsersWithFilters_ReturnAllUsers() {
        UserFilter testUserFilter = new UserFilter(
                null,
                null,
                null,
                null,
                0,
                5
        );
        Pageable testPageable = PageRequest.of(
                testUserFilter.getPage(),
                testUserFilter.getSize()
        );
        Page<UserEntity> testPage = new PageImpl<>(
                List.of(testUserEntity),
                testPageable,
                1L
        );

        when(userRepository.findByFilters(
                null,
                null,
                null,
                null,
                testPageable)
        ).thenReturn(testPage);
        when(objectMapper.convertValue(testUserEntity, User.class))
                .thenReturn(testUser);

        Page<User> result = userService.getAllUsersWithFilters(testUserFilter);

        assertNotNull(result);
        // Проверка контента
        assertEquals(1, result.getContent().size());
        // Проверка пагинации
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(userRepository).findByFilters(
               null,
               null,
               null,
               null,
               testPageable
        );
        verify(objectMapper).convertValue(testUserEntity, User.class);
    }

    @Test
    void getAllUsersWithFilters_ReturnEmptyPage() {
        UserFilter testUserFilter = new UserFilter(
                "Test",
                null,
                null,
                null,
                0,
                5
        );
        Pageable testPageable = PageRequest.of(
                testUserFilter.getPage(),
                testUserFilter.getSize()
        );
        Page<UserEntity> testEmptyPage = new PageImpl<>(
                List.of(),
                testPageable,
                0L
        );

        when(userRepository.findByFilters(
                "Test",
                null,
                null,
                null,
                testPageable)
        ).thenReturn(testEmptyPage);

        Page<User> result = userService.getAllUsersWithFilters(testUserFilter);

        assertNotNull(result);
        // Проверка контента
        assertTrue(result.getContent().isEmpty());
        // Проверка пагинации
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(userRepository).findByFilters(
                "Test",
                null,
                null,
                null,
                testPageable
        );
    }

    @Test
    void createUser_ValidUser_CreateUser() {
        User testUserToCreate = new User(
                null,
                testUser.firstname(),
                testUser.lastname(),
                testUser.age()
        );

        when(userMapper.toUserEntity(testUserToCreate))
                .thenReturn(testUserEntity);
        when(userRepository.save
                (argThat(entity -> entity.getId() == null))
        ).thenReturn(testUserEntity);
        when(objectMapper.convertValue(testUserEntity, User.class))
                .thenReturn(testUser);
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, UserEvent>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(anyString(), anyString(), any(UserEvent.class)))
                .thenReturn(future);

        User result = userService.createUser(testUserToCreate);

        assertNotNull(result);
        assertEquals(testUser, result);

        verify(userMapper).toUserEntity(testUserToCreate);
        verify(userRepository).save(
                (argThat(entity -> entity.getId() == null))
        );
        verify(objectMapper).convertValue(testUserEntity, User.class);
        verify(kafkaTemplate).send(
                eq(TEST_USER_EVENT),
                eq(testId.toString()),
                argThat(userEvent ->
                        "CREATE".equals(userEvent.operation()) &&
                                userEvent.user().equals(testUser)
                )
        );
    }

    @Test
    void createUser_InvalidUser_ThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(testUser)
        );

        verify(userMapper, never()).toUserEntity(any());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(UserEvent.class));
    }

    @Test
    void updateUserById_Exist_UpdateUser() {
        when(userRepository.existsById(testId))
                .thenReturn(true);
        when(userMapper.toUserEntity(testUser))
                .thenReturn(testUserEntity);
        when(userRepository.save(testUserEntity))
                .thenReturn(testUserEntity);
        when(objectMapper.convertValue(testUserEntity, User.class))
                .thenReturn(testUser);
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, UserEvent>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(anyString(), anyString(), any(UserEvent.class)))
                .thenReturn(future);

        User result = userService.updateUserById(testId, testUser);

        assertNotNull(result);
        assertEquals(testUser, result);

        verify(userRepository).existsById(testId);
        verify(userMapper).toUserEntity(testUser);
        verify(userRepository).save(testUserEntity);
        verify(objectMapper).convertValue(testUserEntity, User.class);
        verify(kafkaTemplate).send(
                eq(TEST_USER_EVENT),
                eq(testId.toString()),
                argThat(userEvent ->
                        "UPDATE".equals(userEvent.operation()) &&
                                userEvent.user().equals(testUser)
                )
        );
    }

    @Test
    void updateUserById_NotExist_ThrowException() {
        when(userRepository.existsById(testId)).thenReturn(false);

        assertThrows(NoSuchElementException.class,
                () -> userService.updateUserById(testId, testUser)
        );

        verify(userRepository).existsById(testId);
        verify(userMapper, never()).toUserEntity(any());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(UserEvent.class));
    }

    @Test
    void deleteUserById_Exist_DeleteUser() {
        when(userRepository.findById(testId))
                .thenReturn(Optional.of(testUserEntity));
        when(objectMapper.convertValue(testUserEntity, User.class))
                .thenReturn(testUser);
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, UserEvent>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(anyString(), anyString(), any(UserEvent.class)))
                .thenReturn(future);

        userService.deleteUserById(testId);

        verify(userRepository).findById(testId);
        verify(objectMapper).convertValue(testUserEntity, User.class);
        verify(userRepository).deleteById(testId);
        verify(kafkaTemplate).send(
                eq(TEST_USER_EVENT),
                eq(testId.toString()),
                argThat(userEvent ->
                        "DELETE".equals(userEvent.operation()) &&
                                userEvent.user().equals(testUser))
        );
    }

    @Test
    void deleteUserById_NotExist_ThrowException() {
        when(userRepository.findById(testId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.deleteUserById(testId));

        verify(userRepository).findById(testId);
        verify(userRepository, never()).deleteById(testId);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(UserEvent.class));
    }
}
