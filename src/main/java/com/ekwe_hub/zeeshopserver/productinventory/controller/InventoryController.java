package com.ekwe_hub.zeeshopserver.productinventory.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productinventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryByProduct(productId)));
    }

    @PatchMapping("/{productId}/adjust")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(@PathVariable UUID productId,
                                                                        @Valid @RequestBody AdjustInventoryRequest request) {
        InventoryResponse updated = inventoryService.adjustStock(productId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Stock adjusted successfully"));
    }
}
