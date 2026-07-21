package com.ekwe_hub.zeeshopserver.supplierpurchase.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.SupplierResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.service.interfaces.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private SupplierController supplierController;

    private UUID supplierId;
    private SupplierResponse supplierResponse;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();

        supplierResponse = SupplierResponse.builder()
                .id(supplierId)
                .name("Acme Distributors")
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void getAllSuppliers_returnsOkWithServiceResult() {
        when(supplierService.getAllSuppliers()).thenReturn(List.of(supplierResponse));

        ResponseEntity<ApiResponse<List<SupplierResponse>>> response = supplierController.getAllSuppliers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(supplierResponse);
    }

    @Test
    void getSupplier_returnsOkWithServiceResult() {
        when(supplierService.getSupplier(supplierId)).thenReturn(supplierResponse);

        ResponseEntity<ApiResponse<SupplierResponse>> response = supplierController.getSupplier(supplierId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(supplierResponse);
    }

    @Test
    void getBalance_returnsOkWithServiceResult() {
        when(supplierService.getBalance(supplierId)).thenReturn(new BigDecimal("120.50"));

        ResponseEntity<ApiResponse<BigDecimal>> response = supplierController.getBalance(supplierId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualByComparingTo("120.50");
    }

    @Test
    void createSupplier_returnsCreatedWithServiceResult() {
        CreateSupplierRequest request = new CreateSupplierRequest("Acme Distributors", null, null, null, null);
        when(supplierService.createSupplier(request)).thenReturn(supplierResponse);

        ResponseEntity<ApiResponse<SupplierResponse>> response = supplierController.createSupplier(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(supplierResponse);
        verify(supplierService).createSupplier(request);
    }

    @Test
    void updateSupplier_returnsOkWithServiceResult() {
        UpdateSupplierRequest request = new UpdateSupplierRequest("New Name", null, null, null, null);
        when(supplierService.updateSupplier(supplierId, request)).thenReturn(supplierResponse);

        ResponseEntity<ApiResponse<SupplierResponse>> response = supplierController.updateSupplier(supplierId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(supplierResponse);
        verify(supplierService).updateSupplier(supplierId, request);
    }

    @Test
    void deleteSupplier_returnsOkAndDelegatesToService() {
        ResponseEntity<ApiResponse<Void>> response = supplierController.deleteSupplier(supplierId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(supplierService).deleteSupplier(supplierId);
    }
}
