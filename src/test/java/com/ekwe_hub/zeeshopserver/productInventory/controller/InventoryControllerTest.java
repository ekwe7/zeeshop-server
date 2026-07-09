package com.ekwe_hub.zeeshopserver.productInventory.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.InventoryService;
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
        AdjustInventoryRequest request = new AdjustInventoryRequest(5);
        when(inventoryService.adjustStock(productId, request)).thenReturn(inventoryResponse);

        ResponseEntity<ApiResponse<InventoryResponse>> response = inventoryController.adjustStock(productId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(inventoryResponse);
        verify(inventoryService).adjustStock(productId, request);
    }
}
