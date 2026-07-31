package com.ekwe_hub.zeeshopserver.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSaleRequest(
        @Size(max = 100, message = "Reference number cannot exceed 100 characters")
        String referenceNumber,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes,

        @NotEmpty(message = "Sale must contain at least one item")
        @Valid
        List<SaleItemRequest> items
) {}
