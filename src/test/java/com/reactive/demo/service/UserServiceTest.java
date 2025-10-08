package com.reactive.demo.service;

import com.reactive.demo.dto.EmailMessage;
import com.reactive.demo.model.User;
import com.reactive.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, emailService);
    }

    @Test
    void testGetAllUsers() {
        // Given
        User user1 = new User(1L, "John Doe", "john@example.com");
        User user2 = new User(2L, "Jane Smith", "jane@example.com");
        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2));

        // When & Then
        StepVerifier.create(userService.getAllUsers())
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById() {
        // Given
        User user = new User(1L, "John Doe", "john@example.com");
        when(userRepository.findById(1L)).thenReturn(Mono.just(user));

        // When & Then
        StepVerifier.create(userService.getUserById(1L))
                .expectNext(user)
                .verifyComplete();

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateUser_WithEmailNotification() {
        // Given
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = new User(1L, "John Doe", "john@example.com");
        
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));
        when(emailService.sendEmailAsync(any(EmailMessage.class))).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.createUser(user))
                .expectNext(savedUser)
                .verifyComplete();

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmailAsync(any(EmailMessage.class));
    }

    @Test
    void testCreateUser_EmailFailureDoesNotPreventUserCreation() {
        // Given
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = new User(1L, "John Doe", "john@example.com");
        
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));
        when(emailService.sendEmailAsync(any(EmailMessage.class)))
                .thenReturn(Mono.error(new RuntimeException("Email failed")));

        // When & Then - user should still be created despite email failure
        StepVerifier.create(userService.createUser(user))
                .expectNext(savedUser)
                .verifyComplete();

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmailAsync(any(EmailMessage.class));
    }

    @Test
    void testUpdateUser_WithEmailNotification() {
        // Given
        User existingUser = new User(1L, "John Doe", "john@example.com");
        User updateData = new User(null, "John Updated", "john.updated@example.com");
        User updatedUser = new User(1L, "John Updated", "john.updated@example.com");
        
        when(userRepository.findById(1L)).thenReturn(Mono.just(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));
        when(emailService.sendEmailAsync(any(EmailMessage.class))).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.updateUser(1L, updateData))
                .expectNext(updatedUser)
                .verifyComplete();

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmailAsync(any(EmailMessage.class));
    }

    @Test
    void testDeleteUser_WithEmailNotification() {
        // Given
        User user = new User(1L, "John Doe", "john@example.com");
        
        when(userRepository.findById(1L)).thenReturn(Mono.just(user));
        when(emailService.sendEmailAsync(any(EmailMessage.class))).thenReturn(Mono.empty());
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.deleteUser(1L))
                .verifyComplete();

        verify(userRepository, times(1)).findById(1L);
        verify(emailService, times(1)).sendEmailAsync(any(EmailMessage.class));
        verify(userRepository, times(1)).deleteById(1L);
    }
}
