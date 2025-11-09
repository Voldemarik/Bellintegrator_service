package ru.bellintegrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<User> getById(
            @PathVariable("id") UUID id
    ) {
        log.info("Called getById: id={}", id);
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
    public ResponseEntity<List<User>> getAll() {
        log.info("Called getAll");

//        return userService.getAllUsers();

        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getAllUsers());
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponse<User>> getAllWithPagination(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size
    ) {
        log.info("Called getAllWithPagination: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new PageResponse<>(userService.getAllUsers(pageable)));
    }

    @GetMapping("/filter")
    public ResponseEntity<PageResponse<User>> getAllWithFilters(
            @RequestParam(required = false) String firstname,
            @RequestParam(required = false) String lastname,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size
    ) {
        log.info("Called getAllWithFilters: firstname={}, lastname={}, minAge={}, maxAge={}, page={}, size={}",
                firstname, lastname, minAge, maxAge, page, size);

        UserFilter userFilter = new UserFilter(firstname, lastname, minAge, maxAge, page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new PageResponse<>(userService.getAllUsersWithFilters(userFilter)));
    }

    @PostMapping
    public ResponseEntity<User> create(
            @RequestBody User userToCreate
    ) {
        log.info("Called create");

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
    public ResponseEntity<User> update(
            @PathVariable("id") UUID id,
            @RequestBody User userToUpdate
    ) {
        log.info("Called update: id={} , userToUpdate={}",
                id, userToUpdate);

        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateUserById(id, userToUpdate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") UUID id
    ) {
        log.info("Called deleteById: id={}", id);

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