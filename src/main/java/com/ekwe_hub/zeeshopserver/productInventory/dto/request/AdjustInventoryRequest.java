package com.ekwe_hub.zeeshopserver.productInventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Changes stock on hand by a signed amount: a positive quantity increases
 * stock, a negative quantity decreases it. Kept as a single delta-based
 * operation rather than separate increase/decrease endpoints since both are
 * the same write with opposite sign.
 */
public record AdjustInventoryRequest(

        @NotNull(message = "Quantity is required")
        Integer quantity,

        // Optional free-text note (e.g. "Restock", "Damaged"), kept in inventory history
        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason
) {
}
