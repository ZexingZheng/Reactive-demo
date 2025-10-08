package com.example.reactive.service;

import com.example.reactive.email.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final Scheduler emailScheduler;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    public EmailService(
            JavaMailSender mailSender,
            @Qualifier("emailScheduler") Scheduler emailScheduler) {
        this.mailSender = mailSender;
        this.emailScheduler = emailScheduler;
    }

    /**
     * Send email asynchronously
     * Execute blocking JavaMailSender operations in dedicated thread pool
     */
    public Mono<Void> sendEmail(EmailMessage emailMessage) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled, skipping email to: {}", emailMessage.getTo());
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
                    log.info("Preparing to send email to: {}", emailMessage.getTo());
                    sendEmailSync(emailMessage);
                    return true;
                })
                .subscribeOn(emailScheduler) // Execute in dedicated thread pool
                .timeout(Duration.ofSeconds(30)) // 30 seconds timeout
                .doOnSuccess(result ->
                        log.info("Successfully sent email to: {}", emailMessage.getTo()))
                .doOnError(error ->
                        log.error("Failed to send email to: {}", emailMessage.getTo(), error))
                .onErrorResume(e -> {
                    // Email sending failure does not affect main process
                    log.warn("Email sending failed, continuing...", e);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * Send email synchronously (executed in separate thread pool)
     */
    private void sendEmailSync(EmailMessage emailMessage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailMessage.getTo());
            helper.setSubject(emailMessage.getSubject());
            helper.setText(emailMessage.getContent(), emailMessage.isHtml());

            mailSender.send(mimeMessage);

            log.debug("Email sent successfully to: {}", emailMessage.getTo());
        } catch (MessagingException e) {
            log.error("Failed to create email message", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }


}
