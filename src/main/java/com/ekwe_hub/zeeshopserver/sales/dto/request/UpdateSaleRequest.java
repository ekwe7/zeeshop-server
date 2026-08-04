package com.ekwe_hub.zeeshopserver.sales.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSaleRequest(
        @Size(max = 100, message = "Reference number cannot exceed 100 characters")
        String referenceNumber,

        com.ekwe_hub.zeeshopserver.sales.entity.PaymentType paymentType,

        @Size(max = 150, message = "Customer name cannot exceed 150 characters")
        String customerName,

        @Size(max = 50, message = "Customer phone cannot exceed 50 characters")
        String customerPhone,

        @Size(max = 150, message = "Customer email cannot exceed 150 characters")
        String customerEmail,

        java.time.LocalDate dueDate,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {}
