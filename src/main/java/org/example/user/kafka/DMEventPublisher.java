package org.example.user.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DMEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public DMEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDMEvent(String senderId, String receiverId, String content) {
        String message = String.format("User %s sent DM to User %s: %s", senderId, receiverId, content);
        kafkaTemplate.send("dm-topic", message); // Kafka 토픽에 메시지 발행
    }
}
