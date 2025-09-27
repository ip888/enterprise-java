package com.enterprise.portfolio.userservice.service;

import com.enterprise.portfolio.userservice.events.UserEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event Publisher Service for publishing user events to Kafka
 * 
 * Implements:
 * - Event-driven architecture with Apache Kafka
 * - Asynchronous event publishing
 * - Event serialization and deserialization
 * - Error handling for event publishing
 */
@Service
public class EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    private static final String USER_EVENTS_TOPIC = "user-events";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public EventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish user event to Kafka topic
     */
    public Mono<Void> publishUserEvent(UserEvent event) {
        return Mono.fromCallable(() -> serializeEvent(event))
            .flatMap(serializedEvent -> publishEvent(event.eventType(), serializedEvent))
            .doOnSuccess(v -> logger.info("Successfully published event: {} for user: {}", 
                event.eventType(), event.userId()))
            .doOnError(error -> logger.error("Failed to publish event: {} for user: {}", 
                event.eventType(), event.userId(), error))
            .onErrorComplete(); // Don't fail the main operation if event publishing fails
    }
    
    /**
     * Serialize event to JSON string
     */
    private String serializeEvent(UserEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event: {}", event.eventType(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }
    
    /**
     * Publish event to Kafka topic
     */
    private Mono<Void> publishEvent(String eventType, String serializedEvent) {
        return Mono.fromFuture(
            kafkaTemplate.send(USER_EVENTS_TOPIC, eventType, serializedEvent)
                .toCompletableFuture()
        ).flatMap(result -> Mono.empty());
    }
}