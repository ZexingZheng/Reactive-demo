package com.example.reactive.controller;

import com.example.reactive.entity.User;
import com.example.reactive.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User R2DBC API", description = "用户管理 R2DBC 接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "获取所有用户", description = "查询所有用户列表")
    public Flux<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "根据ID获取用户", description = "通过用户ID查询用户信息")
    public Mono<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "按姓名搜索用户", description = "精确搜索用户姓名")
    public Flux<User> getUsersByName(@RequestParam String name) {
        return userService.getUsersByName(name);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建用户", description = "创建新用户")
    public Mono<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Mono<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "删除用户", description = "删除指定用户")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}
