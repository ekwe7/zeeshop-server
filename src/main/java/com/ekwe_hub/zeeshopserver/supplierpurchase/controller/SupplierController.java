package com.ekwe_hub.zeeshopserver.supplierpurchase.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.SupplierResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.service.interfaces.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getAllSuppliers()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplier(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getSupplier(id)));
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getBalance(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse created = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(@PathVariable UUID id,
                                                                          @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse updated = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Supplier updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable UUID id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Supplier deleted successfully"));
    }
}
