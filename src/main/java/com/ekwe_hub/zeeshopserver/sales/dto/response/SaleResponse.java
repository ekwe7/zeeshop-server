package com.ekwe_hub.zeeshopserver.sales.dto.response;

import com.ekwe_hub.zeeshopserver.sales.entity.PaymentType;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        String referenceNumber,
        SaleStatus status,
        PaymentType paymentType,
        String customerName,
        String customerPhone,
        String customerEmail,
        java.time.LocalDate dueDate,
        String notes,
        BigDecimal totalAmount,
        List<SaleItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
