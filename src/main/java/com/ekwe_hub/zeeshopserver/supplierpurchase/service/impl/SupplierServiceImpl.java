package com.ekwe_hub.zeeshopserver.supplierpurchase.service.impl;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.DuplicateResourceException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.shared.domain.event.DomainEventPublisher;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.SupplierResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Supplier;
import com.ekwe_hub.zeeshopserver.supplierpurchase.event.SupplierCreatedEvent;
import com.ekwe_hub.zeeshopserver.supplierpurchase.mapper.SupplierMapper;
import com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces.PurchaseRepository;
import com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces.SupplierRepository;
import com.ekwe_hub.zeeshopserver.supplierpurchase.service.interfaces.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;
    private final SupplierMapper supplierMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    public SupplierResponse getSupplier(UUID id) {
        return supplierMapper.toResponse(findSupplierOrThrow(id));
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        if (supplierRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Supplier", "name", request.name());
        }

        Supplier supplier = supplierMapper.toEntity(request);
        supplier = supplierRepository.save(supplier);

        domainEventPublisher.publish(new SupplierCreatedEvent(supplier.getId(), supplier.getName()));

        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(UUID id, UpdateSupplierRequest request) {
        Supplier supplier = findSupplierOrThrow(id);

        if (supplierRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Supplier", "name", request.name());
        }

        supplierMapper.updateEntity(request, supplier);
        supplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(UUID id) {
        Supplier supplier = findSupplierOrThrow(id);

        if (purchaseRepository.existsBySupplierId(id)) {
            throw new BusinessRuleViolationException(
                    "Cannot delete supplier '%s' while purchases are recorded against it".formatted(supplier.getName()));
        }

        supplierRepository.delete(supplier);
    }

    @Override
    public BigDecimal getBalance(UUID id) {
        return findSupplierOrThrow(id).getBalance();
    }

    private Supplier findSupplierOrThrow(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }
}
