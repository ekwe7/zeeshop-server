package com.ekwe_hub.zeeshopserver.customerdebt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String phone,
        String email,
        String address,
        BigDecimal totalDebt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
