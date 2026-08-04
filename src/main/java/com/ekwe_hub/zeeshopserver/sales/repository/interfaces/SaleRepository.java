package com.ekwe_hub.zeeshopserver.sales.repository.interfaces;

import com.ekwe_hub.zeeshopserver.sales.entity.PaymentType;
import com.ekwe_hub.zeeshopserver.sales.entity.Sale;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    @Query("SELECT s FROM Sale s WHERE (:status IS NULL OR s.status = :status) AND (:paymentType IS NULL OR s.paymentType = :paymentType)")
    Page<Sale> search(@Param("status") SaleStatus status, @Param("paymentType") PaymentType paymentType, Pageable pageable);
}
