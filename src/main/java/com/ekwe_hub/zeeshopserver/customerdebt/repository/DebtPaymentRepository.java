package com.ekwe_hub.zeeshopserver.customerdebt.repository;

import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DebtPaymentRepository extends JpaRepository<DebtPayment, UUID> {
    List<DebtPayment> findByDebtIdOrderByCreatedAtDesc(UUID debtId);
}
