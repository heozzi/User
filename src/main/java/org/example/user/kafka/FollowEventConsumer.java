package org.example.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.user.entity.FollowEntity;
import org.example.user.entity.UserEntity;
import org.example.user.repository.FollowRepository;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FollowEventConsumer {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "follow-topic", groupId = "follow-group")
    public void consumeFollowEvent(String message) {
        try {
            // ✅ JSON 메시지 파싱
            FollowRequest followRequest = objectMapper.readValue(message, FollowRequest.class);
            String followerId = followRequest.getFollowerId();
            String followeeId = followRequest.getFolloweeId();
            String action = followRequest.getAction();

            System.out.println("Kafka 메시지 수신: " + followerId + " -> " + followeeId + " (Action: " + action + ")");

            // ✅ User ID 기준으로 조회
            UserEntity follower = userRepository.findById(Long.parseLong(followerId))
                    .orElseThrow(() -> new IllegalArgumentException("팔로워를 찾을 수 없습니다."));
            UserEntity followee = userRepository.findById(Long.parseLong(followeeId))
                    .orElseThrow(() -> new IllegalArgumentException("팔로우할 사용자를 찾을 수 없습니다."));

            if ("follow".equals(action)) {
                // ✅ 중복 팔로우 방지
                if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
                    System.out.println("이미 팔로우한 사용자입니다.");
                    return;
                }

                // ✅ 팔로우 정보 저장
                FollowEntity followEntity = new FollowEntity();
                followEntity.setFollower(follower);
                followEntity.setFollowee(followee);
                followRepository.save(followEntity);

                System.out.println("팔로우 정보 저장 완료: " + followerId + " -> " + followeeId);

            } else if ("unfollow".equals(action)) {
                // ✅ 언팔로우 처리 (DB에서 삭제)
                followRepository.deleteByFollowerAndFollowee(follower, followee);
                System.out.println("언팔로우 처리 완료: " + followerId + " -> " + followeeId);
            }

        } catch (Exception e) {
            System.err.println("팔로우 처리 중 오류 발생: " + e.getMessage());
        }
    }
}
