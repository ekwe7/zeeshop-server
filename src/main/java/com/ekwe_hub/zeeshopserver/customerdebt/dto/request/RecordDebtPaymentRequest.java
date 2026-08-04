package com.ekwe_hub.zeeshopserver.customerdebt.dto.request;

import com.ekwe_hub.zeeshopserver.sales.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RecordDebtPaymentRequest(
        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
        BigDecimal amount,

        PaymentType paymentType,

        @Size(max = 100, message = "Reference number cannot exceed 100 characters")
        String referenceNumber,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {}
