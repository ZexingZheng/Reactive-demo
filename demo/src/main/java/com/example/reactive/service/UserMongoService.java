package com.example.reactive.service;

import com.example.reactive.entity.UserMongo;
import com.example.reactive.event.UserEvent;
import com.example.reactive.kafka.ReactiveKafkaProducer;
import com.example.reactive.repository.UserMongoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserMongoService {

    private final UserMongoRepository userMongoRepository;
    private final ReactiveKafkaProducer kafkaProducer;

    public UserMongoService(UserMongoRepository userMongoRepository, ReactiveKafkaProducer kafkaProducer) {
        this.userMongoRepository = userMongoRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Cacheable(value = "users", key = "'all'")
    public Flux<UserMongo> getAllUsers() {
        log.debug("Fetching all users from MongoDB");
        return userMongoRepository.findAll();
    }

    @Cacheable(value = "users", key = "#id")
    public Mono<UserMongo> getUserById(String id) {
        log.debug("Fetching user by id: {}", id);
        return userMongoRepository.findById(id);
    }

    public Flux<UserMongo> getUsersByName(String name) {
        log.debug("Searching users by name: {}", name);
        return userMongoRepository.findByNameContainingIgnoreCase(name);
    }

    @CacheEvict(value = "users", allEntries = true)
    public Mono<UserMongo> createUser(UserMongo user) {
        log.info("Creating user: {}", user);
        return userMongoRepository.save(user)
                .doOnSuccess(savedUser -> {
                    log.info("User created successfully: {}", savedUser);
                    sendUserEvent("CREATE", savedUser);
                })
                .doOnError(error -> log.error("Error creating user", error));
    }

    @CacheEvict(value = "users", allEntries = true)
    public Mono<UserMongo> updateUser(String id, UserMongo user) {
        log.info("Updating user with id: {}", id);
        return userMongoRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setAge(user.getAge());
                    return userMongoRepository.save(existingUser);
                })
                .doOnSuccess(updatedUser -> {
                    log.info("User updated successfully: {}", updatedUser);
                    sendUserEvent("UPDATE", updatedUser);
                })
                .doOnError(error -> log.error("Error updating user", error));
    }

    @CacheEvict(value = "users", allEntries = true)
    public Mono<Void> deleteUser(String id) {
        log.info("Deleting user with id: {}", id);
        return userMongoRepository.findById(id)
                .flatMap(user -> {
                    sendUserEvent("DELETE", user);
                    return userMongoRepository.deleteById(id);
                })
                .doOnSuccess(v -> log.info("User deleted successfully"))
                .doOnError(error -> log.error("Error deleting user", error));
    }

    private void sendUserEvent(String eventType, UserMongo user) {
        UserEvent event = new UserEvent();
        event.setEventType(eventType);
        event.setUserId(user.getId());
        event.setUserName(user.getName());
        event.setUserEmail(user.getEmail());
        event.setUserAge(user.getAge());
        event.setTimestamp(LocalDateTime.now());

        kafkaProducer.sendUserEvent(event)
                .doOnSuccess(v -> log.debug("Kafka event sent: {}", eventType))
                .doOnError(e -> log.warn("Failed to send Kafka event", e))
                .subscribe();
    }
}
