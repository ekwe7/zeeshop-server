package com.ekwe_hub.zeeshopserver.productinventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUnitRequest(

        @NotBlank(message = "Unit name is required")
        @Size(max = 50, message = "Unit name must not exceed 50 characters")
        String name,

        @NotBlank(message = "Unit symbol is required")
        @Size(max = 10, message = "Unit symbol must not exceed 10 characters")
        String symbol
) {
}
