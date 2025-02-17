package org.example.user.repository;

import org.example.user.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    boolean existsByGroupName(String groupName);
}