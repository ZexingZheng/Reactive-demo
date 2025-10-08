package com.example.reactive.controller;

import com.example.reactive.entity.UserMongo;
import com.example.reactive.service.UserMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/mongo/users")
@Tag(name = "User MongoDB API", description = "User management MongoDB interface")
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true", matchIfMissing = false)
public class UserMongoController {

    private final UserMongoService userMongoService;

    public UserMongoController(UserMongoService userMongoService) {
        this.userMongoService = userMongoService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all users", description = "Query all users list")
    public Flux<UserMongo> getAllUsers() {
        return userMongoService.getAllUsers();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get user by ID", description = "Query user info by user ID")
    public Mono<UserMongo> getUserById(@PathVariable String id) {
        return userMongoService.getUserById(id);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search users by name", description = "Fuzzy search by user name")
    public Flux<UserMongo> getUsersByName(@RequestParam String name) {
        return userMongoService.getUsersByName(name);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user", description = "Create new user")
    public Mono<UserMongo> createUser(@RequestBody UserMongo user) {
        return userMongoService.createUser(user);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update user", description = "Update user information")
    public Mono<UserMongo> updateUser(@PathVariable String id, @RequestBody UserMongo user) {
        return userMongoService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user", description = "Delete specified user")
    public Mono<Void> deleteUser(@PathVariable String id) {
        return userMongoService.deleteUser(id);
    }
}
