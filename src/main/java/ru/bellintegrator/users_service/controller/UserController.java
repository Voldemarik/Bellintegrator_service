package ru.bellintegrator.users_service.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bellintegrator.users_service.model.UserDto;
import ru.bellintegrator.users_service.model.UserFilter;
import org.springframework.cloud.openfeign.SpringQueryMap;
import ru.bellintegrator.users_service.service.UserService;

import java.util.*;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable("id") UUID id) {
        log.info("Called getById: id={}", id);
        return ResponseEntity.ok().body(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAll(@SpringQueryMap UserFilter filter) {
        log.info("Called getAll with filter = {}", filter);
        return ResponseEntity.ok().body(userService.getAll(filter));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody UserDto userToCreate) {
        log.info("Called create: userToCreate={}", userToCreate);
        userService.createUser(userToCreate);
        return ResponseEntity.accepted().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody UserDto userToUpdate) {
        log.info("Called update: userToUpdate={}", userToUpdate);
        userService.updateUser(userToUpdate);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        log.info("Called deleteById: id={} ", id);
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
