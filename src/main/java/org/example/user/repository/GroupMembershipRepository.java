package org.example.user.repository;

import org.example.user.entity.GroupMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembershipEntity, Long> {
    List<GroupMembershipEntity> findByGroup_Gid(Long gid);
}