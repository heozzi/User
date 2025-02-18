package org.example.user.repository;

import org.example.user.entity.FollowEntity;
import org.example.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    boolean existsByFollowerAndFollowee(UserEntity follower, UserEntity followee);

    // ✅ 언팔로우 기능 추가
    void deleteByFollowerAndFollowee(UserEntity follower, UserEntity followee);
}
