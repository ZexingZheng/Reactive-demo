package com.example.reactive.service;

import com.example.reactive.entity.User;
import com.example.reactive.event.UserEvent;
import com.example.reactive.kafka.ReactiveKafkaProducer;
import com.example.reactive.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReactiveKafkaProducer kafkaProducer;

    public UserService(UserRepository userRepository, ReactiveKafkaProducer kafkaProducer) {
        this.userRepository = userRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<User> createUser(User user) {
        return userRepository.save(user)
                .flatMap(savedUser -> {
                    // 异步发送 Kafka 消息,不阻塞主流程
                    UserEvent event = UserEvent.create(
                            savedUser.getId(),
                            savedUser.getName(),
                            savedUser.getEmail(),
                            savedUser.getAge()
                    );
                    return kafkaProducer.sendUserEvent(event)
                            .doOnError(e -> log.error("Failed to send CREATE event to Kafka", e))
                            .onErrorResume(e -> Mono.empty()) // 即使 Kafka 失败,也返回成功
                            .thenReturn(savedUser);
                });
    }

    public Mono<User> updateUser(Long id, User user) {
        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setAge(user.getAge());
                    return userRepository.save(existingUser);
                })
                .flatMap(updatedUser -> {
                    // 异步发送 Kafka 消息
                    UserEvent event = UserEvent.update(
                            updatedUser.getId(),
                            updatedUser.getName(),
                            updatedUser.getEmail(),
                            updatedUser.getAge()
                    );
                    return kafkaProducer.sendUserEvent(event)
                            .doOnError(e -> log.error("Failed to send UPDATE event to Kafka", e))
                            .onErrorResume(e -> Mono.empty())
                            .thenReturn(updatedUser);
                });
    }

    public Mono<Void> deleteUser(Long id) {
        return userRepository.deleteById(id)
                .then(Mono.defer(() -> {
                    // 异步发送 Kafka 消息
                    UserEvent event = UserEvent.delete(id);
                    return kafkaProducer.sendUserEvent(event)
                            .doOnError(e -> log.error("Failed to send DELETE event to Kafka", e))
                            .onErrorResume(e -> Mono.empty());
                }));
    }

    public Flux<User> getUsersByName(String name) {
        return userRepository.findByName(name);
    }
}
