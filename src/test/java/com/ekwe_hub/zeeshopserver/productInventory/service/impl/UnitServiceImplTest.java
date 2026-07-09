package com.ekwe_hub.zeeshopserver.productInventory.service.impl;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.DuplicateResourceException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.UnitResponse;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.productInventory.mapper.UnitMapper;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.ProductRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class UnitServiceImplTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UnitMapper unitMapper;

    @InjectMocks
    private UnitServiceImpl unitService;

    private UUID unitId;
    private Unit unit;
    private UnitResponse unitResponse;

    @BeforeEach
    void setUp() {
        unitId = UUID.randomUUID();

        unit = Unit.builder().name("Kilogram").symbol("kg").build();
        unit.setId(unitId);

        unitResponse = UnitResponse.builder()
                .id(unitId)
                .name("Kilogram")
                .symbol("kg")
                .build();
    }

    @Test
    void getAllUnits_mapsEveryPersistedUnit() {
        when(unitRepository.findAll()).thenReturn(List.of(unit));
        when(unitMapper.toResponse(unit)).thenReturn(unitResponse);

        List<UnitResponse> result = unitService.getAllUnits();

        assertThat(result).containsExactly(unitResponse);
    }

    @Test
    void getUnit_returnsMappedResponse_whenUnitExists() {
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(unitMapper.toResponse(unit)).thenReturn(unitResponse);

        UnitResponse result = unitService.getUnit(unitId);

        assertThat(result).isEqualTo(unitResponse);
    }

    @Test
    void getUnit_throwsResourceNotFound_whenUnitMissing() {
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unitService.getUnit(unitId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUnit_savesAndReturnsMappedResponse() {
        CreateUnitRequest request = new CreateUnitRequest("Kilogram", "kg");

        when(unitRepository.existsByName("Kilogram")).thenReturn(false);
        when(unitRepository.existsBySymbol("kg")).thenReturn(false);
        when(unitMapper.toEntity(request)).thenReturn(unit);
        when(unitRepository.save(unit)).thenReturn(unit);
        when(unitMapper.toResponse(unit)).thenReturn(unitResponse);

        UnitResponse result = unitService.createUnit(request);

        assertThat(result).isEqualTo(unitResponse);
        verify(unitRepository).save(unit);
    }

    @Test
    void createUnit_throwsDuplicateResource_whenNameTaken() {
        CreateUnitRequest request = new CreateUnitRequest("Kilogram", "kg");
        when(unitRepository.existsByName("Kilogram")).thenReturn(true);

        assertThatThrownBy(() -> unitService.createUnit(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(unitRepository, never()).save(any());
    }

    @Test
    void createUnit_throwsDuplicateResource_whenSymbolTaken() {
        CreateUnitRequest request = new CreateUnitRequest("Kilogram", "kg");
        when(unitRepository.existsByName("Kilogram")).thenReturn(false);
        when(unitRepository.existsBySymbol("kg")).thenReturn(true);

        assertThatThrownBy(() -> unitService.createUnit(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(unitRepository, never()).save(any());
    }

    @Test
    void updateUnit_delegatesFieldAssignmentToMapper() {
        UpdateUnitRequest request = new UpdateUnitRequest("Gram", "g");

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(unitRepository.existsByNameAndIdNot("Gram", unitId)).thenReturn(false);
        when(unitRepository.existsBySymbolAndIdNot("g", unitId)).thenReturn(false);
        when(unitRepository.save(unit)).thenReturn(unit);
        when(unitMapper.toResponse(unit)).thenReturn(unitResponse);

        UnitResponse result = unitService.updateUnit(unitId, request);

        assertThat(result).isEqualTo(unitResponse);
        verify(unitMapper).updateEntity(request, unit);
        verify(unitRepository).save(unit);
    }

    @Test
    void updateUnit_throwsResourceNotFound_whenUnitMissing() {
        UpdateUnitRequest request = new UpdateUnitRequest("Gram", "g");
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unitService.updateUnit(unitId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(unitRepository, never()).save(any());
    }

    @Test
    void updateUnit_throwsDuplicateResource_whenNameTakenBySomeoneElse() {
        UpdateUnitRequest request = new UpdateUnitRequest("taken-name", "g");
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(unitRepository.existsByNameAndIdNot("taken-name", unitId)).thenReturn(true);

        assertThatThrownBy(() -> unitService.updateUnit(unitId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(unitRepository, never()).save(any());
    }

    @Test
    void updateUnit_throwsDuplicateResource_whenSymbolTakenBySomeoneElse() {
        UpdateUnitRequest request = new UpdateUnitRequest("Gram", "taken-symbol");
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(unitRepository.existsByNameAndIdNot("Gram", unitId)).thenReturn(false);
        when(unitRepository.existsBySymbolAndIdNot("taken-symbol", unitId)).thenReturn(true);

        assertThatThrownBy(() -> unitService.updateUnit(unitId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(unitRepository, never()).save(any());
    }

    @Test
    void deleteUnit_deletesUnit_whenNotReferencedByAnyProduct() {
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(productRepository.existsByUnitId(unitId)).thenReturn(false);

        unitService.deleteUnit(unitId);

        verify(unitRepository).delete(unit);
    }

    @Test
    void deleteUnit_throwsBusinessRuleViolation_whenReferencedByProduct() {
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(productRepository.existsByUnitId(unitId)).thenReturn(true);

        assertThatThrownBy(() -> unitService.deleteUnit(unitId))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(unitRepository, never()).delete(any());
    }

    @Test
    void deleteUnit_throwsResourceNotFound_whenUnitMissing() {
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unitService.deleteUnit(unitId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(unitRepository, never()).delete(any());
    }
}
