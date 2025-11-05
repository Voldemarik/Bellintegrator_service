package ru.bellintegrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable("id") UUID id
    ) {
        log.info("Called getUserById: id={}", id);
//        return userService.getUserById(id);
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(userService.getUserById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getaAllTasks() {
        log.info("Called getAllUsers");
//        return userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @RequestBody User userToCreate
    ) {
        log.info("Called createUser");
//        return userService.createUser(userToCreate);
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(userService.createUser(userToCreate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUserById(
            @PathVariable("id") UUID id,
            @RequestBody User userToUpdate
    ) {
        log.info("Called updateUserById: id={} , userToUpdate={}",
                id, userToUpdate);
        var updated = userService.updateUserById(id, userToUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(
            @PathVariable("id") UUID id
    ) {
        log.info("Called deleteUserById: id={}", id);
        try {
            userService.deleteUserById(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
}
