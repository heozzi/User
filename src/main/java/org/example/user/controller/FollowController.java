package org.example.user.controller;

import org.example.user.entity.UserEntity;
import org.example.user.kafka.FollowEventPublisher;
import org.example.user.kafka.FollowRequest;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowEventPublisher followEventPublisher;
    private final UserRepository userRepository; // 추가

    @Autowired
    public FollowController(FollowEventPublisher followEventPublisher, UserRepository userRepository) {
        this.followEventPublisher = followEventPublisher;
        this.userRepository = userRepository;
    }

    // 팔로우 API
    @PostMapping
    public ResponseEntity<String> followUser(@RequestBody FollowRequest followRequest,
                                             @RequestHeader("Authorization") String token) {
        try {
            // 팔로워(로그인한 사용자)와 팔로이(팔로우 대상) 모두 존재하는지 확인
            UserEntity follower = userRepository.findById(Long.parseLong(followRequest.getFollowerId()))
                    .orElseThrow(() -> new IllegalArgumentException("팔로워(요청자)를 찾을 수 없습니다."));

            UserEntity followee = userRepository.findById(Long.parseLong(followRequest.getFolloweeId()))
                    .orElseThrow(() -> new IllegalArgumentException("팔로우 대상자를 찾을 수 없습니다."));

            // 두 사용자 모두 활성화된 상태인지 확인
            if (!follower.isEnabled() || !followee.isEnabled()) {
                throw new IllegalStateException("비활성화된 사용자입니다.");
            }

            // 자기 자신을 팔로우하는지 확인
            if (follower.getUid().equals(followee.getUid())) {
                throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
            }

            // 팔로우 이벤트 발행
            followEventPublisher.publishFollowEvent(followRequest.getFollowerId(), followRequest.getFolloweeId());
            return ResponseEntity.ok("Successfully followed user " + followRequest.getFolloweeId());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("팔로우 처리 중 오류가 발생했습니다.");
        }
    }

    // 언팔로우 API
    @DeleteMapping
    public ResponseEntity<String> unfollowUser(@RequestBody FollowRequest followRequest,
                                               @RequestHeader("Authorization") String token) {
        try {
            // 팔로워(로그인한 사용자)와 팔로이(언팔로우 대상) 모두 존재하는지 확인
            UserEntity follower = userRepository.findById(Long.parseLong(followRequest.getFollowerId()))
                    .orElseThrow(() -> new IllegalArgumentException("팔로워(요청자)를 찾을 수 없습니다."));

            UserEntity followee = userRepository.findById(Long.parseLong(followRequest.getFolloweeId()))
                    .orElseThrow(() -> new IllegalArgumentException("언팔로우 대상자를 찾을 수 없습니다."));

            // 두 사용자 모두 활성화된 상태인지 확인
            if (!follower.isEnabled() || !followee.isEnabled()) {
                throw new IllegalStateException("비활성화된 사용자입니다.");
            }

            // 자기 자신을 언팔로우하는지 확인
            if (follower.getUid().equals(followee.getUid())) {
                throw new IllegalArgumentException("자기 자신을 언팔로우할 수 없습니다.");
            }

            // 언팔로우 이벤트 발행 로직 추가 필요
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

