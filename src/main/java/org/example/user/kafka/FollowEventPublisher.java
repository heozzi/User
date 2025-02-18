package org.example.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FollowEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public FollowEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishFollowEvent(String followerId, String followeeId, String action) {
        try {
            // ✅ JSON 형식으로 Kafka 메시지 전송
            FollowRequest followRequest = new FollowRequest();
            followRequest.setFollowerId(followerId);
            followRequest.setFolloweeId(followeeId);
            followRequest.setAction(action);

            String jsonMessage = objectMapper.writeValueAsString(followRequest);
            kafkaTemplate.send("follow-topic", jsonMessage);

            System.out.println("Kafka 메시지 발행: " + jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
