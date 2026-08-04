package com.ekwe_hub.zeeshopserver.customerdebt.repository;

import com.ekwe_hub.zeeshopserver.customerdebt.entity.CustomerDebt;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerDebtRepository extends JpaRepository<CustomerDebt, UUID> {

    Optional<CustomerDebt> findBySaleId(UUID saleId);

    @Query("""
        SELECT d FROM CustomerDebt d
        WHERE (:status IS NULL OR d.status = :status)
          AND (:search IS NULL OR LOWER(d.customerName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(d.customerPhone) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(d.customerEmail) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<CustomerDebt> search(
            @Param("status") DebtStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
