package org.example.user.controller;

import org.example.user.entity.UserEntity;
import org.example.user.kafka.FollowEventPublisher;
import org.example.user.kafka.FollowRequest;
import org.example.user.repository.UserRepository;
import org.example.user.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowEventPublisher followEventPublisher;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Autowired
    public FollowController(FollowEventPublisher followEventPublisher, UserRepository userRepository, FollowRepository followRepository) {
        this.followEventPublisher = followEventPublisher;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    // ✅ 팔로우 API
    @PostMapping
    public ResponseEntity<String> followUser(@RequestBody FollowRequest followRequest) {
        try {
            // 팔로워(요청자)와 팔로이(대상)가 존재하는지 확인
            UserEntity follower = userRepository.findById(Long.parseLong(followRequest.getFollowerId()))
                    .orElseThrow(() -> new IllegalArgumentException("팔로워(요청자)를 찾을 수 없습니다."));

            UserEntity followee = userRepository.findById(Long.parseLong(followRequest.getFolloweeId()))
                    .orElseThrow(() -> new IllegalArgumentException("팔로우 대상자를 찾을 수 없습니다."));

            // 두 사용자 모두 활성화 상태인지 확인
            if (!follower.isEnabled() || !followee.isEnabled()) {
                throw new IllegalStateException("비활성화된 사용자입니다.");
            }

            // 자기 자신을 팔로우하는지 확인
            if (follower.getUid().equals(followee.getUid())) {
                throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
            }

            // ✅ Kafka를 통해 팔로우 이벤트 발행 (action: "follow")
            followEventPublisher.publishFollowEvent(followRequest.getFollowerId(), followRequest.getFolloweeId(), "follow");

            return ResponseEntity.ok("Successfully followed user " + followRequest.getFolloweeId());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("팔로우 처리 중 오류가 발생했습니다.");
        }
    }

    // ✅ 언팔로우 API
    @DeleteMapping
    @Transactional  // 트랜잭션을 추가하여 DB 작업을 안전하게 처리
    public ResponseEntity<String> unfollowUser(@RequestBody FollowRequest followRequest) {
        try {
            // 팔로워(요청자)와 팔로이(대상)가 존재하는지 확인
            UserEntity follower = userRepository.findById(Long.parseLong(followRequest.getFollowerId()))
                    .orElseThrow(() -> new IllegalArgumentException("팔로워(요청자)를 찾을 수 없습니다."));

            UserEntity followee = userRepository.findById(Long.parseLong(followRequest.getFolloweeId()))
                    .orElseThrow(() -> new IllegalArgumentException("언팔로우 대상자를 찾을 수 없습니다."));

            // 두 사용자 모두 활성화 상태인지 확인
            if (!follower.isEnabled() || !followee.isEnabled()) {
                throw new IllegalStateException("비활성화된 사용자입니다.");
            }

            // 자기 자신을 언팔로우하는지 확인
            if (follower.getUid().equals(followee.getUid())) {
                throw new IllegalArgumentException("자기 자신을 언팔로우할 수 없습니다.");
            }

            // Follow 엔티티에서 삭제
            followRepository.deleteByFollowerAndFollowee(follower, followee);

            // ✅ Kafka를 통해 언팔로우 이벤트 발행 (action: "unfollow")
            followEventPublisher.publishFollowEvent(followRequest.getFollowerId(), followRequest.getFolloweeId(), "unfollow");

            return ResponseEntity.ok("Successfully unfollowed user " + followRequest.getFolloweeId());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("언팔로우 처리 중 오류가 발생했습니다.");
        }
    }
}
