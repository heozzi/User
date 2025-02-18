package org.example.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "followee_id"}) // 중복 방지
})
public class FollowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팔로우 하는 유저
    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private UserEntity follower;

    // 팔로우 당하는 유저
    @ManyToOne
    @JoinColumn(name = "followee_id", nullable = false)
    private UserEntity followee;

}
