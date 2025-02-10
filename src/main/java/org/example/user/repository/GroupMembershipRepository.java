package org.example.user.repository;

import org.example.user.entity.GroupMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembershipEntity, Long> {
}