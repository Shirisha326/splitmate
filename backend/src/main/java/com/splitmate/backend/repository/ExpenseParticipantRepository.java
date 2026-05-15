package com.splitmate.backend.repository;

import com.splitmate.backend.entity.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {

    List<ExpenseParticipant> findByExpenseId(Long expenseId);

    @Query("SELECT COALESCE(SUM(ep.shareAmount), 0) FROM ExpenseParticipant ep WHERE ep.expense.group.id = :groupId AND ep.user.id = :userId")
    BigDecimal sumShareAmountByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Query("SELECT ep FROM ExpenseParticipant ep WHERE ep.expense.group.id = :groupId")
    List<ExpenseParticipant> findAllByGroupId(@Param("groupId") Long groupId);
}
