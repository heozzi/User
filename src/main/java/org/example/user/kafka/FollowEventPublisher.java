package org.example.user.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FollowEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public FollowEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFollowEvent(String followerId, String followeeId) {
        String message = String.format("User %s followed User %s", followerId, followeeId);
        kafkaTemplate.send("follow-topic", message); // Kafka 토픽에 메시지 발행
    }
}
