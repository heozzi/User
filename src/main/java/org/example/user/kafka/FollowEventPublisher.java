package org.example.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.user.kafka.FollowRequest;
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
            // FollowRequest 객체 생성 (User 마이크로서비스에서 정의)
            FollowRequest followRequest = new FollowRequest(followerId, followeeId, action);
            // JSON 문자열로 변환
            String jsonMessage = objectMapper.writeValueAsString(followRequest);
            // "follow" 토픽에 메시지 발행
            kafkaTemplate.send("follow", jsonMessage);
            System.out.println("Kafka Follow 메시지 발행: " + jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Follow 이벤트 Kafka 발행 실패");
        }
    }
}
