package com.ekwe_hub.zeeshopserver.supplierpurchase.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.PurchaseItemRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.ReceiveItemRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.ReceiveStockRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.PurchaseResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseStatus;
import com.ekwe_hub.zeeshopserver.supplierpurchase.service.interfaces.PurchaseService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock
    private PurchaseService purchaseService;

    @InjectMocks
    private PurchaseController purchaseController;

    private UUID purchaseId;
    private UUID supplierId;
    private PurchaseResponse purchaseResponse;

    @BeforeEach
    void setUp() {
        purchaseId = UUID.randomUUID();
        supplierId = UUID.randomUUID();

        purchaseResponse = PurchaseResponse.builder()
                .id(purchaseId)
                .supplierId(supplierId)
                .status(PurchaseStatus.PENDING)
                .totalAmount(new BigDecimal("20.00"))
                .build();
    }

    @Test
    void getAllPurchases_returnsOkWithServiceResult() {
        Pageable pageable = PageRequest.of(0, 20);
        PageResponse<PurchaseResponse> page = PageResponse.<PurchaseResponse>builder()
                .content(List.of(purchaseResponse))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        when(purchaseService.getAllPurchases(supplierId, PurchaseStatus.PENDING, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<PurchaseResponse>>> response =
                purchaseController.getAllPurchases(supplierId, PurchaseStatus.PENDING, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().content()).containsExactly(purchaseResponse);
    }

    @Test
    void getPurchase_returnsOkWithServiceResult() {
        when(purchaseService.getPurchase(purchaseId)).thenReturn(purchaseResponse);

        ResponseEntity<ApiResponse<PurchaseResponse>> response = purchaseController.getPurchase(purchaseId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(purchaseResponse);
    }

    @Test
    void createPurchase_returnsCreatedWithServiceResult() {
        CreatePurchaseRequest request = new CreatePurchaseRequest(supplierId, null, null,
                List.of(new PurchaseItemRequest(UUID.randomUUID(), 10, new BigDecimal("2.00"))));
        when(purchaseService.createPurchase(request)).thenReturn(purchaseResponse);

        ResponseEntity<ApiResponse<PurchaseResponse>> response = purchaseController.createPurchase(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(purchaseResponse);
        verify(purchaseService).createPurchase(request);
    }

    @Test
    void updatePurchase_returnsOkWithServiceResult() {
        UpdatePurchaseRequest request = new UpdatePurchaseRequest("INV-2", "notes");
        when(purchaseService.updatePurchase(purchaseId, request)).thenReturn(purchaseResponse);

        ResponseEntity<ApiResponse<PurchaseResponse>> response = purchaseController.updatePurchase(purchaseId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(purchaseResponse);
        verify(purchaseService).updatePurchase(purchaseId, request);
    }

    @Test
    void deletePurchase_returnsOkAndDelegatesToService() {
        ResponseEntity<ApiResponse<Void>> response = purchaseController.deletePurchase(purchaseId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(purchaseService).deletePurchase(purchaseId);
    }

    @Test
    void receiveStock_returnsOkWithServiceResult() {
        ReceiveStockRequest request = new ReceiveStockRequest(List.of(new ReceiveItemRequest(UUID.randomUUID(), 4)));
        when(purchaseService.receiveStock(purchaseId, request)).thenReturn(purchaseResponse);

        ResponseEntity<ApiResponse<PurchaseResponse>> response = purchaseController.receiveStock(purchaseId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(purchaseResponse);
        verify(purchaseService).receiveStock(purchaseId, request);
    }

    @Test
    void cancelPurchase_returnsOkWithServiceResult() {
        when(purchaseService.cancelPurchase(purchaseId)).thenReturn(purchaseResponse);

        ResponseEntity<ApiResponse<PurchaseResponse>> response = purchaseController.cancelPurchase(purchaseId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(purchaseResponse);
        verify(purchaseService).cancelPurchase(purchaseId);
    }
}
