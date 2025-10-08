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
@Tag(name = "User MongoDB API", description = "用户管理 MongoDB 接口")
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true", matchIfMissing = false)
public class UserMongoController {

    private final UserMongoService userMongoService;

    public UserMongoController(UserMongoService userMongoService) {
        this.userMongoService = userMongoService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "获取所有用户", description = "查询所有用户列表")
    public Flux<UserMongo> getAllUsers() {
        return userMongoService.getAllUsers();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "根据ID获取用户", description = "通过用户ID查询用户信息")
    public Mono<UserMongo> getUserById(@PathVariable String id) {
        return userMongoService.getUserById(id);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "按姓名搜索用户", description = "模糊搜索用户姓名")
    public Flux<UserMongo> getUsersByName(@RequestParam String name) {
        return userMongoService.getUsersByName(name);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建用户", description = "创建新用户")
    public Mono<UserMongo> createUser(@RequestBody UserMongo user) {
        return userMongoService.createUser(user);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Mono<UserMongo> updateUser(@PathVariable String id, @RequestBody UserMongo user) {
        return userMongoService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "删除用户", description = "删除指定用户")
    public Mono<Void> deleteUser(@PathVariable String id) {
        return userMongoService.deleteUser(id);
    }
}
