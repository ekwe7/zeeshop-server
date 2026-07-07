package com.ekwe_hub.zeeshopserver.productinventory.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Changes stock on hand by a signed amount: a positive quantity increases
 * stock, a negative quantity decreases it. Kept as a single delta-based
 * operation rather than separate increase/decrease endpoints since both are
 * the same write with opposite sign.
 */
public record AdjustInventoryRequest(

        @NotNull(message = "Quantity is required")
        Integer quantity
) {
}
