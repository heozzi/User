package org.example.user.controller;

import org.example.user.kafka.FollowEventPublisher;
import org.example.user.kafka.FollowRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowEventPublisher followEventPublisher;

    @Autowired
    public FollowController(FollowEventPublisher followEventPublisher) {
        this.followEventPublisher = followEventPublisher;
    }

    // 팔로우 API
    @PostMapping
    public String followUser(@RequestBody FollowRequest followRequest,
                             @RequestHeader("Authorization") String token) {
        // JWT 토큰 인증 및 사용자 확인
        System.out.println("Token: " + token);  // 로그로 확인

        // 팔로우 이벤트 발행
        followEventPublisher.publishFollowEvent(followRequest.getFollowerId(), followRequest.getFolloweeId());
        return "Followed " + followRequest.getFolloweeId();
    }

    // 언팔로우 API
    @DeleteMapping
    public String unfollowUser(@RequestBody FollowRequest followRequest,
                               @RequestHeader("Authorization") String token) {
        // JWT 토큰 인증 및 사용자 확인
        System.out.println("Token: " + token);  // 로그로 확인

        // 언팔로우 처리 (DB에서 제거 등 추가 로직 필요)
        return "Unfollowed " + followRequest.getFolloweeId();
    }
}

