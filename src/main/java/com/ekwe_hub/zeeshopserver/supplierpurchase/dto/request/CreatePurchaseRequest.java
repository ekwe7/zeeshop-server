package com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreatePurchaseRequest(

        @NotNull(message = "Supplier is required")
        UUID supplierId,

        @Size(max = 100, message = "Invoice number must not exceed 100 characters")
        String invoiceNumber,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes,

        @NotEmpty(message = "A purchase must have at least one item")
        @Valid
        List<PurchaseItemRequest> items
) {
}
