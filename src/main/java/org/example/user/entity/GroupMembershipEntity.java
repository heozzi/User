package org.example.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_membership")
@Data
@NoArgsConstructor
public class GroupMembershipEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gid")
    private GroupEntity group;

    @ManyToOne
    @JoinColumn(name = "uid")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private MemberRole role; // ADMIN, MEMBER

    public enum MemberRole {
        ADMIN, MEMBER
    }
}
