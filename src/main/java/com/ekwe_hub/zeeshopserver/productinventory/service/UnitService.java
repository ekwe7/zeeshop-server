package com.ekwe_hub.zeeshopserver.productinventory.service;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.DuplicateResourceException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.CreateUnitRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.UpdateUnitRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.UnitResponse;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.productinventory.mapper.UnitMapper;
import com.ekwe_hub.zeeshopserver.productinventory.repository.ProductRepository;
import com.ekwe_hub.zeeshopserver.productinventory.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CRUD for units of measure. Deletion is blocked while any Product still
 * references the unit, mirroring CategoryService's guard for the same reason:
 * Product.unit is a required relationship.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitService {

    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;
    private final UnitMapper unitMapper;

    public List<UnitResponse> getAllUnits() {
        return unitRepository.findAll().stream()
                .map(unitMapper::toResponse)
                .toList();
    }

    public UnitResponse getUnit(UUID id) {
        return unitMapper.toResponse(findUnitOrThrow(id));
    }

    @Transactional
    public UnitResponse createUnit(CreateUnitRequest request) {
        if (unitRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Unit", "name", request.name());
        }
        if (unitRepository.existsBySymbol(request.symbol())) {
            throw new DuplicateResourceException("Unit", "symbol", request.symbol());
        }

        Unit unit = unitMapper.toEntity(request);
        unit = unitRepository.save(unit);
        return unitMapper.toResponse(unit);
    }

    @Transactional
    public UnitResponse updateUnit(UUID id, UpdateUnitRequest request) {
        Unit unit = findUnitOrThrow(id);

        if (unitRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Unit", "name", request.name());
        }
        if (unitRepository.existsBySymbolAndIdNot(request.symbol(), id)) {
            throw new DuplicateResourceException("Unit", "symbol", request.symbol());
        }

        unitMapper.updateEntity(request, unit);
        unit = unitRepository.save(unit);
        return unitMapper.toResponse(unit);
    }

    @Transactional
    public void deleteUnit(UUID id) {
        Unit unit = findUnitOrThrow(id);

        if (productRepository.existsByUnitId(id)) {
            throw new BusinessRuleViolationException(
                    "Cannot delete unit '%s' while it is still assigned to a product".formatted(unit.getName()));
        }

        unitRepository.delete(unit);
    }

    private Unit findUnitOrThrow(UUID id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
    }
}
