package com.ekwe_hub.zeeshopserver.productInventory.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.UnitResponse;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.UnitService;
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

import java.util.List;
import java.util.UUID;

/**
 * CRUD for units of measure. Guarded by the INVENTORY_READ/INVENTORY_WRITE
 * permissions, same as ProductController and CategoryController.
 */
@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<List<UnitResponse>>> getAllUnits() {
        return ResponseEntity.ok(ApiResponse.success(unitService.getAllUnits()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<UnitResponse>> getUnit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(unitService.getUnit(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(@Valid @RequestBody CreateUnitRequest request) {
        UnitResponse created = unitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<ApiResponse<UnitResponse>> updateUnit(@PathVariable UUID id,
                                                                  @Valid @RequestBody UpdateUnitRequest request) {
        UnitResponse updated = unitService.updateUnit(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Unit updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable UUID id) {
        unitService.deleteUnit(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Unit deleted successfully"));
    }
}
