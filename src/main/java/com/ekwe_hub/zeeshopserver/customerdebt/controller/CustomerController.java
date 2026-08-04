package com.ekwe_hub.zeeshopserver.customerdebt.controller;

import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.CreateCustomerRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.RecordDebtPaymentRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.UpdateCustomerRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerDebtResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.DebtPaymentResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtStatus;
import com.ekwe_hub.zeeshopserver.customerdebt.service.interfaces.CustomerDebtService;
import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerDebtService customerDebtService;

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> searchCustomers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerDebtService.searchCustomers(query, pageable)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        return ResponseEntity.ok(ApiResponse.success(customerDebtService.getAllCustomers()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerDebtService.getCustomer(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse created = customerDebtService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse updated = customerDebtService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Customer updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        customerDebtService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer deleted successfully"));
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getCustomerBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerDebtService.getCustomerBalance(id)));
    }

    @GetMapping("/{id}/debts")
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<List<CustomerDebtResponse>>> getCustomerDebtHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerDebtService.getCustomerDebtHistory(id)));
    }
}
