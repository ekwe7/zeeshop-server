package com.ekwe_hub.zeeshopserver.productInventory.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.SetLowStockThresholdRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryAdjustmentResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private UUID productId;
    private InventoryResponse inventoryResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        inventoryResponse = InventoryResponse.builder()
                .productId(productId)
                .productName("Coke")
                .sku("SKU-001")
                .quantityOnHand(10)
                .lowStockThreshold(3)
                .build();
    }

    @Test
    void getAllInventory_returnsOkWithServiceResult() {
        when(inventoryService.getAllInventory()).thenReturn(List.of(inventoryResponse));

        ResponseEntity<ApiResponse<List<InventoryResponse>>> response = inventoryController.getAllInventory();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(inventoryResponse);
    }

    @Test
    void getInventoryByProduct_returnsOkWithServiceResult() {
        when(inventoryService.getInventoryByProduct(productId)).thenReturn(inventoryResponse);

        ResponseEntity<ApiResponse<InventoryResponse>> response = inventoryController.getInventoryByProduct(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(inventoryResponse);
    }

    @Test
    void adjustStock_returnsOkWithServiceResult() {
        AdjustInventoryRequest request = new AdjustInventoryRequest(5, "Restock");
        when(inventoryService.adjustStock(productId, request)).thenReturn(inventoryResponse);

        ResponseEntity<ApiResponse<InventoryResponse>> response = inventoryController.adjustStock(productId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(inventoryResponse);
        verify(inventoryService).adjustStock(productId, request);
    }

    @Test
    void getLowStockInventory_returnsOkWithServiceResult() {
        when(inventoryService.getLowStockInventory()).thenReturn(List.of(inventoryResponse));

        ResponseEntity<ApiResponse<List<InventoryResponse>>> response = inventoryController.getLowStockInventory();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(inventoryResponse);
    }

    @Test
    void updateLowStockThreshold_returnsOkWithServiceResult() {
        SetLowStockThresholdRequest request = new SetLowStockThresholdRequest(8);
        when(inventoryService.updateLowStockThreshold(productId, request)).thenReturn(inventoryResponse);

        ResponseEntity<ApiResponse<InventoryResponse>> response =
                inventoryController.updateLowStockThreshold(productId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(inventoryResponse);
        verify(inventoryService).updateLowStockThreshold(productId, request);
    }

    @Test
    void getInventoryHistory_returnsOkWithServiceResult() {
        Pageable pageable = PageRequest.of(0, 20);
        PageResponse<InventoryAdjustmentResponse> page = PageResponse.<InventoryAdjustmentResponse>builder()
                .content(List.of(InventoryAdjustmentResponse.builder().productId(productId).build()))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        when(inventoryService.getInventoryHistory(productId, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<InventoryAdjustmentResponse>>> response =
                inventoryController.getInventoryHistory(productId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().content()).hasSize(1);
    }
}
