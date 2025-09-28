package com.edtech.repository;

import com.edtech.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(String status);

    List<Payment> findByUserIdAndStatus(Long userId, String status);
}