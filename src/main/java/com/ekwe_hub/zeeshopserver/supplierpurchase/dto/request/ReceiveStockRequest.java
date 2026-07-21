package com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReceiveStockRequest(

        @NotEmpty(message = "At least one item must be received")
        @Valid
        List<ReceiveItemRequest> items
) {
}
