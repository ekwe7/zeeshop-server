package com.ekwe_hub.zeeshopserver.productInventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String name,

        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        String sku,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price must not be negative")
        BigDecimal price,

        @NotNull(message = "Category is required")
        UUID categoryId,

        @NotNull(message = "Unit is required")
        UUID unitId,

        // Defaults to 0 when omitted
        @Min(value = 0, message = "Initial quantity must not be negative")
        Integer initialQuantity,

        // Defaults to 0 (not tracked) when omitted
        @Min(value = 0, message = "Low stock threshold must not be negative")
        Integer lowStockThreshold
) {
}
