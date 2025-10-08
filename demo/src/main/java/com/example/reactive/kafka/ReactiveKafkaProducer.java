package com.example.reactive.kafka;

import com.example.reactive.event.UserEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Service
public class ReactiveKafkaProducer {

    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "user-events";

    public ReactiveKafkaProducer(KafkaSender<String, String> kafkaSender) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Mono<Void> sendUserEvent(UserEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(json -> {
                    ProducerRecord<String, String> record =
                        new ProducerRecord<>(TOPIC, event.getUserId().toString(), json);

                    SenderRecord<String, String, String> senderRecord =
                        SenderRecord.create(record, event.getUserId().toString());

                    return kafkaSender.send(Mono.just(senderRecord))
                            .next()
                            .doOnSuccess(result ->
                                log.info("Successfully sent message to topic: {}, partition: {}, offset: {}",
                                    TOPIC,
                                    result.recordMetadata().partition(),
                                    result.recordMetadata().offset()))
                            .doOnError(error ->
                                log.error("Failed to send message to Kafka", error))
                            .then();
                })
                .onErrorResume(JsonProcessingException.class, e -> {
                    log.error("Failed to serialize event", e);
                    return Mono.empty();
                });
    }
}
