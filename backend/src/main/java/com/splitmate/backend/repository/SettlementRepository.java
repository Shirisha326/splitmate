package com.splitmate.backend.repository;

import com.splitmate.backend.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId AND s.status = 'COMPLETED' ORDER BY s.settledAt DESC")
    List<Settlement> findCompletedByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId AND (s.fromUser.id = :userId OR s.toUser.id = :userId) ORDER BY s.createdAt DESC")
    List<Settlement> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
