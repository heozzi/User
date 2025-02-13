package org.example.user.repository;

import org.example.user.entity.GroupMembershipEntity;
import org.example.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembershipEntity, Long> {
    List<GroupMembershipEntity> findByGroup_Gid(Long gid);
    void deleteByUser(UserEntity user); // 특정 유저의 그룹 멤버십을 삭제
    List<GroupMembershipEntity> findByUser(UserEntity user); // 프로필 조회
}