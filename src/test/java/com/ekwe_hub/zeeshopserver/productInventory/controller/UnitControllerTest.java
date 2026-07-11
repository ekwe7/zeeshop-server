package com.ekwe_hub.zeeshopserver.productInventory.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.UnitResponse;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitControllerTest {

    @Mock
    private UnitService unitService;

    @InjectMocks
    private UnitController unitController;

    private UUID unitId;
    private UnitResponse unitResponse;

    @BeforeEach
    void setUp() {
        unitId = UUID.randomUUID();

        unitResponse = UnitResponse.builder()
                .id(unitId)
                .name("Kilogram")
                .symbol("kg")
                .build();
    }

    @Test
    void getAllUnits_returnsOkWithServiceResult() {
        when(unitService.getAllUnits()).thenReturn(List.of(unitResponse));

        ResponseEntity<ApiResponse<List<UnitResponse>>> response = unitController.getAllUnits();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(unitResponse);
    }

    @Test
    void getUnit_returnsOkWithServiceResult() {
        when(unitService.getUnit(unitId)).thenReturn(unitResponse);

        ResponseEntity<ApiResponse<UnitResponse>> response = unitController.getUnit(unitId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(unitResponse);
    }

    @Test
    void createUnit_returnsCreatedWithServiceResult() {
        CreateUnitRequest request = new CreateUnitRequest("Kilogram", "kg");
        when(unitService.createUnit(request)).thenReturn(unitResponse);

        ResponseEntity<ApiResponse<UnitResponse>> response = unitController.createUnit(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(unitResponse);
        verify(unitService).createUnit(request);
    }

    @Test
    void updateUnit_returnsOkWithServiceResult() {
        UpdateUnitRequest request = new UpdateUnitRequest("Gram", "g");
        when(unitService.updateUnit(unitId, request)).thenReturn(unitResponse);

        ResponseEntity<ApiResponse<UnitResponse>> response = unitController.updateUnit(unitId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(unitResponse);
        verify(unitService).updateUnit(unitId, request);
    }

    @Test
    void deleteUnit_returnsOkAndDelegatesToService() {
        ResponseEntity<ApiResponse<Void>> response = unitController.deleteUnit(unitId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(unitService).deleteUnit(unitId);
    }
}
