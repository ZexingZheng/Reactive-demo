package com.reactive.demo.service;

import com.reactive.demo.dto.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Optimized Email Service with the following features:
 * 1. Async processing - emails sent asynchronously without blocking main thread
 * 2. Batching - emails are batched together to reduce SMTP connections
 * 3. Reactive streams - uses Project Reactor for non-blocking operations
 * 4. Connection pooling - JavaMailSender internally pools connections
 * 5. Error handling - graceful error handling with retry logic
 * 6. Buffering - uses buffer to collect emails before sending
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ConcurrentLinkedQueue<EmailMessage> emailQueue;
    
    @Value("${email.batch.size:10}")
    private int batchSize;
    
    @Value("${email.batch.window.seconds:5}")
    private int windowSeconds;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.emailQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Send email asynchronously using reactive approach
     * Optimized by using dedicated scheduler for blocking I/O operations
     */
    public Mono<Void> sendEmailAsync(EmailMessage emailMessage) {
        return Mono.fromRunnable(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(emailMessage.getTo());
                message.setSubject(emailMessage.getSubject());
                message.setText(emailMessage.getBody());
                
                mailSender.send(message);
                log.info("Email sent successfully to: {}", emailMessage.getTo());
            } catch (Exception e) {
                log.error("Failed to send email to: {}", emailMessage.getTo(), e);
                throw new RuntimeException("Email sending failed", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then()
        .retry(3); // Retry up to 3 times on failure
    }

    /**
     * Queue email for batch processing
     * Optimized by collecting emails and sending in batches
     */
    public Mono<Void> queueEmail(EmailMessage emailMessage) {
        return Mono.fromRunnable(() -> emailQueue.offer(emailMessage))
                .then()
                .doOnSuccess(v -> log.debug("Email queued for batch processing: {}", emailMessage.getTo()));
    }

    /**
     * Process queued emails in batches
     * Optimized using reactive bufferTimeout for efficient batching
     */
    public Flux<Void> processBatchedEmails() {
        return Flux.fromIterable(emailQueue)
                .bufferTimeout(batchSize, Duration.ofSeconds(windowSeconds))
                .flatMap(this::sendBatch)
                .doOnComplete(() -> log.info("Batch email processing completed"));
    }

    /**
     * Send a batch of emails
     * Optimized by sending multiple emails in parallel with controlled concurrency
     */
    private Mono<Void> sendBatch(List<EmailMessage> emails) {
        if (emails.isEmpty()) {
            return Mono.empty();
        }

        log.info("Sending batch of {} emails", emails.size());
        
        return Flux.fromIterable(emails)
                .flatMap(email -> sendEmailAsync(email)
                        .doOnSuccess(v -> emailQueue.remove(email))
                        .onErrorResume(e -> {
                            log.error("Error sending email in batch: {}", email.getTo(), e);
                            return Mono.empty(); // Continue with other emails
                        }), 
                        5) // Limit concurrency to 5 to avoid overwhelming mail server
                .then();
    }

    /**
     * Traditional async method using Spring's @Async
     * Kept for backward compatibility and simple use cases
     */
    @Async
    public void sendEmailSync(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Sync email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send sync email to: {}", to, e);
        }
    }

    /**
     * Send multiple emails efficiently using reactive streams
     * Optimized by processing in parallel with controlled concurrency
     */
    public Mono<Void> sendBulkEmails(List<EmailMessage> emails) {
        int effectiveBatchSize = batchSize > 0 ? batchSize : 10;
        return Flux.fromIterable(emails)
                .buffer(effectiveBatchSize)
                .flatMap(batch -> Flux.fromIterable(batch)
                        .flatMap(this::sendEmailAsync, 5)
                        .then(), 2) // Process 2 batches in parallel
                .then()
                .doOnSuccess(v -> log.info("Bulk email sending completed for {} emails", emails.size()));
    }
}
