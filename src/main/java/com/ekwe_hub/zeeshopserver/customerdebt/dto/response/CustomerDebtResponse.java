package com.ekwe_hub.zeeshopserver.customerdebt.dto.response;

import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerDebtResponse(
        UUID id,
        UUID customerId,
        UUID saleId,
        String customerName,
        String customerPhone,
        String customerEmail,
        BigDecimal initialAmount,
        BigDecimal amount,
        BigDecimal paidAmount,
        LocalDate dueDate,
        DebtStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
