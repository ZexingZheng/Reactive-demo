package com.reactive.demo.service;

import com.reactive.demo.dto.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
    }

    @Test
    void testSendEmailAsync_Success() {
        // Given
        EmailMessage emailMessage = new EmailMessage(
                "test@example.com",
                "Test Subject",
                "Test Body"
        );

        // When & Then
        StepVerifier.create(emailService.sendEmailAsync(emailMessage))
                .verifyComplete();

        // Verify mail sender was called
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("test@example.com", capturedMessage.getTo()[0]);
        assertEquals("Test Subject", capturedMessage.getSubject());
        assertEquals("Test Body", capturedMessage.getText());
    }

    @Test
    void testSendEmailAsync_WithRetry() {
        // Given
        EmailMessage emailMessage = new EmailMessage(
                "test@example.com",
                "Test Subject",
                "Test Body"
        );
        
        // Simulate failure twice, then success
        doThrow(new RuntimeException("SMTP error"))
                .doThrow(new RuntimeException("SMTP error"))
                .doNothing()
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        StepVerifier.create(emailService.sendEmailAsync(emailMessage))
                .verifyComplete();

        // Should be called 3 times (2 failures + 1 success)
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testQueueEmail_Success() {
        // Given
        EmailMessage emailMessage = new EmailMessage(
                "test@example.com",
                "Test Subject",
                "Test Body"
        );

        // When & Then
        StepVerifier.create(emailService.queueEmail(emailMessage))
                .verifyComplete();
    }

    @Test
    void testSendBulkEmails_Success() {
        // Given
        List<EmailMessage> emails = Arrays.asList(
                new EmailMessage("user1@example.com", "Subject 1", "Body 1"),
                new EmailMessage("user2@example.com", "Subject 2", "Body 2"),
                new EmailMessage("user3@example.com", "Subject 3", "Body 3")
        );

        // When & Then
        StepVerifier.create(emailService.sendBulkEmails(emails))
                .verifyComplete();

        // Verify all emails were sent
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailSync() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        emailService.sendEmailSync(to, subject, body);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(1000).times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(body, capturedMessage.getText());
    }

    @Test
    void testSendEmailAsync_HandlesException() {
        // Given
        EmailMessage emailMessage = new EmailMessage(
                "test@example.com",
                "Test Subject",
                "Test Body"
        );
        
        // Simulate persistent failure
        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then - should fail after retries
        StepVerifier.create(emailService.sendEmailAsync(emailMessage))
                .expectError(RuntimeException.class)
                .verify();

        // Should retry 3 times (original + 3 retries = 4 total)
        verify(mailSender, times(4)).send(any(SimpleMailMessage.class));
    }
}
