package ru.bellintegrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.bellintegrator.UserService;
import ru.bellintegrator.User;
import ru.bellintegrator.UserController;
import ru.bellintegrator.UserFilter;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

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

    private UUID testId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testUser = new User(
                testId,
                "Екатерина",
                "Сидорова",
                30
        );
    }

    @Test
    void getById_Exist_ReturnUser() throws Exception{
        when(userService.getUserById(testId))
                .thenReturn(testUser);

        mockMvc.perform(get("/users/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstname").value(testUser.firstname()))
                .andExpect(jsonPath("$.lastname").value(testUser.lastname()))
                .andExpect(jsonPath("$.age").value(testUser.age()));

        verify(userService).getUserById(testId);
    }

    @Test
    void getById_NotExist_Return404() throws Exception{
        when(userService.getUserById(testId))
                .thenThrow(NoSuchElementException.class);

        mockMvc.perform(get("/users/{id}", testId))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(testId);
    }

    @Test
    void getAll_ReturnListOfUsers() throws Exception{
        List<User> testUserList = List.of(testUser);

        when(userService.getAllUsers())
                .thenReturn(testUserList);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].firstname").value(testUser.firstname()))
                .andExpect(jsonPath("$[0].lastname").value(testUser.lastname()))
                .andExpect(jsonPath("$[0].age").value(testUser.age()));

        verify(userService).getAllUsers();
    }

    @Test
    void getAllWithPagination_ReturnPageOfUsers() throws Exception {
        Pageable testPageable = PageRequest.of(0, 5);
        Page<User> testUserPage = new PageImpl<>(
                List.of(testUser),
                testPageable,
                1L
        );

        when(userService.getAllUsers(testPageable))
                .thenReturn(testUserPage);

        mockMvc.perform(get("/users/page")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(testId.toString()))
                .andExpect(jsonPath("$.content[0].firstname").value(testUser.firstname()))
                .andExpect(jsonPath("$.content[0].lastname").value(testUser.lastname()))
                .andExpect(jsonPath("$.content[0].age").value(testUser.age()))

                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(userService).getAllUsers(testPageable);
    }

    @Test
    void getAllWithFilters_ReturnPageOfFilteredUsers() throws Exception {
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
        Page<User> testFilteredUserPage = new PageImpl<>(
                List.of(testUser),
                testPageable,
                1L
        );

        when(userService.getAllUsersWithFilters(
                argThat(filter ->
                        "Екатер".equals(filter.getFirstname()) &&
                                "Сидор".equals(filter.getLastname()) &&
                                filter.getMinAge() == 20 &&
                                filter.getMaxAge() == 30 &&
                                filter.getPage() == 0 &&
                                filter.getSize() == 5
                )
        ))
                .thenReturn(testFilteredUserPage);

        mockMvc.perform(get("/users/filter")
                        .param("firstname", testUserFilter.getFirstname())
                        .param("lastname", testUserFilter.getLastname())
                        .param("minAge", testUserFilter.getMinAge().toString())
                        .param("maxAge", testUserFilter.getMaxAge().toString())
                        .param("page", testUserFilter.getPage().toString())
                        .param("size", testUserFilter.getSize().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(testId.toString()))
                .andExpect(jsonPath("$.content[0].firstname").value(testUser.firstname()))
                .andExpect(jsonPath("$.content[0].lastname").value(testUser.lastname()))
                .andExpect(jsonPath("$.content[0].age").value(testUser.age()))

                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(userService).getAllUsersWithFilters(
                argThat(filter ->
                        "Екатер".equals(filter.getFirstname()) &&
                        "Сидор".equals(filter.getLastname()) &&
                        filter.getMinAge() == 20 &&
                        filter.getMaxAge() == 30 &&
                        filter.getPage() == 0 &&
                        filter.getSize() == 5
        ));
    }

    @Test
    void create_ValidUser_CreateUser() throws Exception {
        User testUserToCreate = new User(
                null,
                testUser.firstname(),
                testUser.lastname(),
                testUser.age()
        );

        when(userService.createUser(testUserToCreate))
                .thenReturn(testUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstname").value(testUser.firstname()))
                .andExpect(jsonPath("$.lastname").value(testUser.lastname()))
                .andExpect(jsonPath("$.age").value(testUser.age()));

        verify(userService).createUser(testUserToCreate);
    }

    @Test
    void create_InvalidUser_Return400() throws Exception {
        when(userService.createUser(testUser))
                .thenThrow(IllegalArgumentException.class);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest());

        verify(userService).createUser(testUser);
    }

    @Test
    void update_Exist_UpdateUser() throws Exception {
        User userToUpdate = new User(
                null,
                testUser.firstname(),
                testUser.lastname(),
                testUser.age()
        );

        when(userService.updateUserById(testId, userToUpdate))
                .thenReturn(testUser);

        mockMvc.perform(put("/users/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstname").value(testUser.firstname()))
                .andExpect(jsonPath("$.lastname").value(testUser.lastname()))
                .andExpect(jsonPath("$.age").value(testUser.age()));

        verify(userService).updateUserById(testId, userToUpdate);
    }

    @Test
    void update_NotExist_Return404() throws Exception {
        when(userService.updateUserById(testId, testUser))
                .thenThrow(NoSuchElementException.class);

        mockMvc.perform(put("/users/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());

        verify(userService).updateUserById(testId, testUser);
    }

    @Test
    void delete_Exist_DeleteUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", testId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserById(testId);
    }

    @Test
    void delete_NotExist_Return404() throws Exception{
        doThrow(NoSuchElementException.class)
                .when(userService).deleteUserById(testId);;

        mockMvc.perform(delete("/users/{id}", testId))
                .andExpect(status().isNotFound());

        verify(userService).deleteUserById(testId);
    }

}
