package com.example.reactive.kafka;

import com.example.reactive.email.EmailMessage;
import com.example.reactive.event.UserEvent;
import com.example.reactive.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;

@Slf4j
@Service
public class ReactiveKafkaConsumer {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private Disposable disposable;

    public ReactiveKafkaConsumer(
            KafkaReceiver<String, String> kafkaReceiver,
            EmailService emailService) {
        this.kafkaReceiver = kafkaReceiver;
        this.emailService = emailService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void startConsuming() {
        disposable = kafkaReceiver.receive()
                .flatMap(this::processRecord)
                .doOnError(error -> log.error("Error consuming from Kafka", error))
                .retry()
                .subscribe();

        log.info("Started Kafka reactive consumer for topic: user-events");
    }

    private Mono<Void> processRecord(ReceiverRecord<String, String> record) {
        return Mono.fromCallable(() -> objectMapper.readValue(record.value(), UserEvent.class))
                .flatMap(event -> {
                    log.info("Received user event: type={}, userId={}, timestamp={}",
                            event.getEventType(),
                            event.getUserId(),
                            event.getTimestamp());

                    // Process event asynchronously (including sending email)
                    return processEvent(event)
                            .doOnSuccess(v -> {
                                // Manually commit offset after successful processing
                                record.receiverOffset().acknowledge();
                                log.debug("Acknowledged offset for userId: {}", event.getUserId());
                            });
                })
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(error -> {
                    log.error("Failed to process record, skipping...", error);
                    // Acknowledge even on failure to avoid infinite reprocessing
                    record.receiverOffset().acknowledge();
                    return Mono.empty();
                });
    }

    private Mono<Void> processEvent(UserEvent event) {
        return switch (event.getEventType()) {
            case "CREATE" -> {
                log.info("Processing CREATE event for user: {}", event.getUserName());
                EmailMessage email = EmailMessage.createUserWelcomeEmail(
                        event.getUserEmail(), event.getUserName());
                yield emailService.sendEmail(email);
            }
            case "UPDATE" -> {
                log.info("Processing UPDATE event for user: {}", event.getUserName());
                EmailMessage email = EmailMessage.createUserUpdateEmail(
                        event.getUserEmail(), event.getUserName());
                yield emailService.sendEmail(email);
            }
            case "DELETE" -> {
                log.info("Processing DELETE event for userId: {}", event.getUserId());
                // No email info in DELETE event, need to get from elsewhere or skip
                // For demonstration, we simply log
                log.info("Skipping email for DELETE event (no email available)");
                yield Mono.empty();
            }
            default -> {
                log.warn("Unknown event type: {}", event.getEventType());
                yield Mono.empty();
            }
        };
    }

    @PreDestroy
    public void stopConsuming() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            log.info("Stopped Kafka reactive consumer");
        }
    }
}
