package com.ekwe_hub.zeeshopserver.productInventory.service.interfaces;

import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.UnitResponse;

import java.util.List;
import java.util.UUID;

/**
 * CRUD for units of measure. Deletion is blocked while any Product still
 * references the unit, mirroring CategoryService's guard for the same reason:
 * Product.unit is a required relationship.
 */
public interface UnitService {

    List<UnitResponse> getAllUnits();

    UnitResponse getUnit(UUID id);

    UnitResponse createUnit(CreateUnitRequest request);

    UnitResponse updateUnit(UUID id, UpdateUnitRequest request);

    void deleteUnit(UUID id);
}
