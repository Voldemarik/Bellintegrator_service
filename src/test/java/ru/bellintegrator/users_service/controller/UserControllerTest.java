package ru.bellintegrator.users_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.bellintegrator.users_service.model.UserDto;
import ru.bellintegrator.users_service.model.UserFilter;
import ru.bellintegrator.users_service.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setId(userId);
        testUserDto.setFirstname("Ivan");
        testUserDto.setLastname("Ivanov");
        testUserDto.setAge(18);
    }

    @Test
    void getById_ShouldReturnUserDto_WhenUserExists() throws Exception {
        when(userService.getUserById(userId)).thenReturn(testUserDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstname").value("Ivan"))
                .andExpect(jsonPath("$.lastname").value("Ivanov"))
                .andExpect(jsonPath("$.age").value(18));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getAll_ShouldReturnPageOfUsers() throws Exception {
        List<UserDto> userList = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(userList, PageRequest.of(0, 10), 1);

        when(userService.getAll(any(UserFilter.class))).thenReturn(userPage);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(userId.toString()))
                .andExpect(jsonPath("$.content[0].firstname").value("Ivan"))
                .andExpect(jsonPath("$.content[0].lastname").value("Ivanov"))
                .andExpect(jsonPath("$.content[0].age").value(18));

        verify(userService, times(1)).getAll(any(UserFilter.class));
    }

    @Test
    void create_ShouldReturnAccepted() throws Exception {
        UserDto userToCreate = new UserDto();
        userToCreate.setFirstname("New");
        userToCreate.setLastname("User");

        doNothing().when(userService).createUser(any(UserDto.class));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isAccepted());

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void update_ShouldReturnAccepted() throws Exception {
        UserDto userToUpdate = new UserDto();
        userToUpdate.setId(userId);
        userToUpdate.setFirstname("UpdatedName");

//        doNothing().when(userService).updateUser(any(UserDto.class));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isAccepted());

        verify(userService, times(1)).updateUser(any(UserDto.class));
    }

    @Test
    void deleteById_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUserById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(userId);
    }
}