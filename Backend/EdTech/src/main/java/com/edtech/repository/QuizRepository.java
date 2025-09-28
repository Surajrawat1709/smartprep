package com.edtech.repository;

import com.edtech.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUserId(Long userId);

    List<Quiz> findByQuestionBankId(Long questionBankId);

    List<Quiz> findByUserIdAndQuestionBankId(Long userId, Long questionBankId);
}