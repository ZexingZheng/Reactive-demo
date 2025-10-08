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
     * 异步发送邮件
     * 将阻塞的 JavaMailSender 操作放到专用线程池中执行
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
                .subscribeOn(emailScheduler) // 在专用线程池中执行
                .timeout(Duration.ofSeconds(30)) // 30秒超时
                .doOnSuccess(result ->
                        log.info("Successfully sent email to: {}", emailMessage.getTo()))
                .doOnError(error ->
                        log.error("Failed to send email to: {}", emailMessage.getTo(), error))
                .onErrorResume(e -> {
                    // 邮件发送失败不影响主流程
                    log.warn("Email sending failed, continuing...", e);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 同步发送邮件（在独立线程池中执行）
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
