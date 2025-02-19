package org.example.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.user.kafka.DMRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DMEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DMEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDMEvent(String senderId, String receiverId, String content) {
        try {
            // DMRequest 객체 생성 (User 마이크로서비스에서 정의)
            DMRequest dmRequest = new DMRequest(senderId, receiverId, content);
            // JSON 문자열로 변환
            String jsonMessage = objectMapper.writeValueAsString(dmRequest);
            // "dm" 토픽에 메시지 발행
            kafkaTemplate.send("dm", jsonMessage);
            System.out.println("Kafka DM 메시지 발행: " + jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
