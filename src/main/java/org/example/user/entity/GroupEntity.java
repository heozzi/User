package org.example.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groups_info")
@Data
@NoArgsConstructor
public class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gid;

    @Column(nullable = false)
    private String groupName;

    @Column
    private String groupDetail;

    // 그룹 멤버십, 채팅방 등과의 관계 매핑 필요
}
