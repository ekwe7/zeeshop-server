package com.ekwe_hub.zeeshopserver.customerdebt.dto.response;

import com.ekwe_hub.zeeshopserver.sales.entity.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DebtPaymentResponse(
        UUID id,
        UUID debtId,
        BigDecimal amount,
        PaymentType paymentType,
        String referenceNumber,
        String notes,
        LocalDateTime createdAt
) {}
