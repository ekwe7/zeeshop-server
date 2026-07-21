package com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Only invoiceNumber/notes are editable after creation — supplier and items
 * are fixed once a purchase exists (see PurchaseService.updatePurchase).
 */
public record UpdatePurchaseRequest(

        @Size(max = 100, message = "Invoice number must not exceed 100 characters")
        String invoiceNumber,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
