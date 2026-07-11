package com.ekwe_hub.zeeshopserver.productInventory.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.SetLowStockThresholdRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryAdjustmentResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Stock-level views and adjustments. No create/delete here — an Inventory
 * record is provisioned/retired by ProductController alongside its Product
 * (see ProductService).
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventory() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAllInventory()));
    }

    // Registered ahead of /{productId} — Spring ranks literal segments above path
    // variables regardless of declaration order, but this keeps the routes readable.
    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockInventory() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockInventory()));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryByProduct(productId)));
    }

    @GetMapping("/{productId}/history")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<PageResponse<InventoryAdjustmentResponse>>> getInventoryHistory(
            @PathVariable UUID productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryHistory(productId, pageable)));
    }

    @PatchMapping("/{productId}/adjust")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(@PathVariable UUID productId,
                                                                        @Valid @RequestBody AdjustInventoryRequest request) {
        InventoryResponse updated = inventoryService.adjustStock(productId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Stock adjusted successfully"));
    }

    @PatchMapping("/{productId}/threshold")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateLowStockThreshold(
            @PathVariable UUID productId,
            @Valid @RequestBody SetLowStockThresholdRequest request) {
        InventoryResponse updated = inventoryService.updateLowStockThreshold(productId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Low stock threshold updated successfully"));
    }
}
