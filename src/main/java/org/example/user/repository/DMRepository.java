package org.example.user.repository;

import org.example.user.entity.DMEntity;
import org.example.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DMRepository extends JpaRepository<DMEntity, Long> {
    List<DMEntity> findBySenderAndReceiver(UserEntity sender, UserEntity receiver);
    List<DMEntity> findByReceiverAndIsReadFalse(UserEntity receiver);
}
