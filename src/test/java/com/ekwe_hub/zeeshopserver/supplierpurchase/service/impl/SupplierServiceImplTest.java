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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private UUID supplierId;
    private Supplier supplier;
    private SupplierResponse supplierResponse;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();

        supplier = Supplier.builder().name("Acme Distributors").balance(BigDecimal.ZERO).build();
        supplier.setId(supplierId);

        supplierResponse = SupplierResponse.builder()
                .id(supplierId)
                .name("Acme Distributors")
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void getAllSuppliers_mapsEveryPersistedSupplier() {
        when(supplierRepository.findAll()).thenReturn(List.of(supplier));
        when(supplierMapper.toResponse(supplier)).thenReturn(supplierResponse);

        List<SupplierResponse> result = supplierService.getAllSuppliers();

        assertThat(result).containsExactly(supplierResponse);
    }

    @Test
    void getSupplier_returnsMappedResponse_whenSupplierExists() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toResponse(supplier)).thenReturn(supplierResponse);

        SupplierResponse result = supplierService.getSupplier(supplierId);

        assertThat(result).isEqualTo(supplierResponse);
    }

    @Test
    void getSupplier_throwsResourceNotFound_whenSupplierMissing() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getSupplier(supplierId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSupplier_savesAndPublishesEvent() {
        CreateSupplierRequest request = new CreateSupplierRequest("Acme Distributors", null, null, null, null);

        when(supplierRepository.existsByName("Acme Distributors")).thenReturn(false);
        when(supplierMapper.toEntity(request)).thenReturn(supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toResponse(supplier)).thenReturn(supplierResponse);

        SupplierResponse result = supplierService.createSupplier(request);

        assertThat(result).isEqualTo(supplierResponse);
        verify(supplierRepository).save(supplier);
        verify(domainEventPublisher).publish(any(SupplierCreatedEvent.class));
    }

    @Test
    void createSupplier_throwsDuplicateResource_whenNameTaken() {
        CreateSupplierRequest request = new CreateSupplierRequest("Acme Distributors", null, null, null, null);
        when(supplierRepository.existsByName("Acme Distributors")).thenReturn(true);

        assertThatThrownBy(() -> supplierService.createSupplier(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(supplierRepository, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void updateSupplier_delegatesFieldAssignmentToMapper() {
        UpdateSupplierRequest request = new UpdateSupplierRequest("New Name", null, null, null, null);

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.existsByNameAndIdNot("New Name", supplierId)).thenReturn(false);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toResponse(supplier)).thenReturn(supplierResponse);

        SupplierResponse result = supplierService.updateSupplier(supplierId, request);

        assertThat(result).isEqualTo(supplierResponse);
        verify(supplierMapper).updateEntity(request, supplier);
        verify(supplierRepository).save(supplier);
    }

    @Test
    void updateSupplier_throwsDuplicateResource_whenNameTakenBySomeoneElse() {
        UpdateSupplierRequest request = new UpdateSupplierRequest("taken-name", null, null, null, null);
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.existsByNameAndIdNot("taken-name", supplierId)).thenReturn(true);

        assertThatThrownBy(() -> supplierService.updateSupplier(supplierId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(supplierRepository, never()).save(any());
    }

    @Test
    void deleteSupplier_deletesSupplier_whenNoPurchasesRecorded() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(purchaseRepository.existsBySupplierId(supplierId)).thenReturn(false);

        supplierService.deleteSupplier(supplierId);

        verify(supplierRepository).delete(supplier);
    }

    @Test
    void deleteSupplier_throwsBusinessRuleViolation_whenPurchasesRecorded() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(purchaseRepository.existsBySupplierId(supplierId)).thenReturn(true);

        assertThatThrownBy(() -> supplierService.deleteSupplier(supplierId))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(supplierRepository, never()).delete(any());
    }

    @Test
    void getBalance_returnsSupplierBalance() {
        supplier.setBalance(new BigDecimal("450.00"));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        BigDecimal result = supplierService.getBalance(supplierId);

        assertThat(result).isEqualByComparingTo("450.00");
    }
}
