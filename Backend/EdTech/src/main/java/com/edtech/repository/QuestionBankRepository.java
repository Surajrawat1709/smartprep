package com.edtech.repository;

import com.edtech.entity.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {
    List<QuestionBank> findByUserId(Long userId);

    List<QuestionBank> findBySubject(String subject);

    List<QuestionBank> findByUserIdAndSubject(Long userId, String subject);
}