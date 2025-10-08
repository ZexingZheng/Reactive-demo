package com.reactive.demo.service;

import com.reactive.demo.dto.EmailMessage;
import com.reactive.demo.model.User;
import com.reactive.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<User> createUser(User user) {
        return userRepository.save(user)
                .flatMap(savedUser -> {
                    // Send welcome email asynchronously
                    EmailMessage email = new EmailMessage(
                            savedUser.getEmail(),
                            "Welcome to Reactive Demo!",
                            String.format("Hello %s, welcome to our platform!", savedUser.getName())
                    );
                    return emailService.sendEmailAsync(email)
                            .thenReturn(savedUser)
                            .onErrorResume(e -> {
                                log.error("Failed to send welcome email, but user created", e);
                                return Mono.just(savedUser); // Don't fail user creation if email fails
                            });
                })
                .doOnSuccess(u -> log.info("User created with email notification: {}", u.getEmail()));
    }

    public Mono<User> updateUser(Long id, User user) {
        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(user.getEmail());
                    return userRepository.save(existingUser);
                })
                .flatMap(updatedUser -> {
                    // Send update notification email
                    EmailMessage email = new EmailMessage(
                            updatedUser.getEmail(),
                            "Profile Updated",
                            String.format("Hello %s, your profile has been updated successfully.", updatedUser.getName())
                    );
                    return emailService.sendEmailAsync(email)
                            .thenReturn(updatedUser)
                            .onErrorResume(e -> {
                                log.error("Failed to send update email, but user updated", e);
                                return Mono.just(updatedUser);
                            });
                })
                .doOnSuccess(u -> log.info("User updated with email notification: {}", u.getEmail()));
    }

    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    // Send goodbye email before deletion
                    EmailMessage email = new EmailMessage(
                            user.getEmail(),
                            "Account Deleted",
                            String.format("Hello %s, your account has been deleted. We're sad to see you go!", user.getName())
                    );
                    return emailService.sendEmailAsync(email)
                            .then(userRepository.deleteById(id))
                            .onErrorResume(e -> {
                                log.error("Failed to send deletion email, but proceeding with deletion", e);
                                return userRepository.deleteById(id);
                            });
                })
                .doOnSuccess(v -> log.info("User deleted with email notification"));
    }
}
