package com.enterprise.java.streaming.util;

import com.enterprise.java.streaming.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Kafka deserializer for UserEvent objects.
 */
public class KafkaEventDeserializer implements Deserializer<UserEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventDeserializer.class);
    private final ObjectMapper objectMapper;
    
    public KafkaEventDeserializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration needed
    }
    
    @Override
    public UserEvent deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        
        try {
            String jsonString = new String(data);
            return objectMapper.readValue(jsonString, UserEvent.class);
        } catch (Exception e) {
            logger.error("Error deserializing UserEvent from topic {}: {}", topic, e.getMessage());
            return null;
        }
    }
    
    @Override
    public void close() {
        // No resources to close
    }
}