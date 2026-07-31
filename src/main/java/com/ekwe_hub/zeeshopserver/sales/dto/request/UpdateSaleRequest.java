package com.ekwe_hub.zeeshopserver.sales.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSaleRequest(
        @Size(max = 100, message = "Reference number cannot exceed 100 characters")
        String referenceNumber,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {}
