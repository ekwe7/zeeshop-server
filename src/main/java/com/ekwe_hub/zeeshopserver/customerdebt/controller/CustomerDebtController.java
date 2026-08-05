package com.ekwe_hub.zeeshopserver.customerdebt.controller;

import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.RecordDebtPaymentRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerDebtResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.DebtPaymentResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtStatus;
import com.ekwe_hub.zeeshopserver.customerdebt.service.interfaces.CustomerDebtService;
import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class CustomerDebtController {

    private final CustomerDebtService customerDebtService;

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<PageResponse<CustomerDebtResponse>>> getAllDebts(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) DebtStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerDebtService.getAllDebts(customerId, status, search, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<CustomerDebtResponse>> getDebt(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerDebtService.getDebt(id)));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<DebtPaymentResponse>> recordPayment(
            @PathVariable UUID id,
            @Valid @RequestBody RecordDebtPaymentRequest request) {
        DebtPaymentResponse payment = customerDebtService.recordPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success(payment, "Debt payment recorded successfully"));
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<List<DebtPaymentResponse>>> getPaymentHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerDebtService.getPaymentHistory(id)));
    }
}
