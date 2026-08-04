package com.ekwe_hub.zeeshopserver.customerdebt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank(message = "Customer name is required")
        @Size(max = 150, message = "Customer name cannot exceed 150 characters")
        String name,

        @Size(max = 16, message = "Phone cannot exceed 16 characters")
        String phone,

        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address
) {}
