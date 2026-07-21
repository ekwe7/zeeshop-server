package com.ekwe_hub.zeeshopserver.supplierpurchase.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.ReceiveStockRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.PurchaseResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseStatus;
import com.ekwe_hub.zeeshopserver.supplierpurchase.service.interfaces.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Purchase CRUD plus its lifecycle actions (receive stock, cancel). A single
 * purchase doubles as its own invoice and, via getAllPurchases' supplierId
 * filter, as supplier purchase history — see PurchaseResponse.
 */
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * supplierId/status are optional filters, ANDed together when present —
     * pass supplierId alone to view a single supplier's purchase history.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseResponse>>> getAllPurchases(
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) PurchaseStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                purchaseService.getAllPurchases(supplierId, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<ApiResponse<PurchaseResponse>> getPurchase(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseService.getPurchase(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<ApiResponse<PurchaseResponse>> createPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        PurchaseResponse created = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<ApiResponse<PurchaseResponse>> updatePurchase(@PathVariable UUID id,
                                                                          @Valid @RequestBody UpdatePurchaseRequest request) {
        PurchaseResponse updated = purchaseService.updatePurchase(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Purchase updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<ApiResponse<Void>> deletePurchase(@PathVariable UUID id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase deleted successfully"));
    }

    @PatchMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<ApiResponse<PurchaseResponse>> receiveStock(@PathVariable UUID id,
                                                                        @Valid @RequestBody ReceiveStockRequest request) {
        PurchaseResponse updated = purchaseService.receiveStock(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Stock received successfully"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<ApiResponse<PurchaseResponse>> cancelPurchase(@PathVariable UUID id) {
        PurchaseResponse updated = purchaseService.cancelPurchase(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Purchase cancelled successfully"));
    }
}
