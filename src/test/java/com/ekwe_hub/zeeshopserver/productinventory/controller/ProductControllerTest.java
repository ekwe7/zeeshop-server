package com.ekwe_hub.zeeshopserver.productinventory.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.CreateProductRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.UpdateProductRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.ProductResponse;
import com.ekwe_hub.zeeshopserver.productinventory.service.ProductService;
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

/**
 * Pure unit tests for the HTTP layer: ProductService is mocked, so these only
 * verify that ProductController delegates correctly and shapes the
 * ResponseEntity/ApiResponse as expected. No Spring context, no MockMvc,
 * no security filter chain — authorization (@PreAuthorize) is Spring
 * Security's concern, not the controller's.
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private UUID productId;
    private UUID categoryId;
    private UUID unitId;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        unitId = UUID.randomUUID();

        productResponse = ProductResponse.builder()
                .id(productId)
                .name("Coke")
                .sku("SKU-001")
                .description("Soft drink")
                .price(BigDecimal.valueOf(1.5))
                .categoryName("Beverages")
                .unitName("Kilogram")
                .quantityOnHand(10)
                .build();
    }

    @Test
    void getAllProducts_returnsOkWithServiceResult() {
        Pageable pageable = PageRequest.of(0, 20);
        PageResponse<ProductResponse> page = PageResponse.<ProductResponse>builder()
                .content(List.of(productResponse))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        when(productService.getAllProducts(null, null, null, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> response =
                productController.getAllProducts(null, null, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().content()).containsExactly(productResponse);
    }

    @Test
    void getProduct_returnsOkWithServiceResult() {
        when(productService.getProduct(productId)).thenReturn(productResponse);

        ResponseEntity<ApiResponse<ProductResponse>> response = productController.getProduct(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(productResponse);
    }

    @Test
    void createProduct_returnsCreatedWithServiceResult() {
        CreateProductRequest request = new CreateProductRequest(
                "Coke", "SKU-001", "Soft drink", BigDecimal.valueOf(1.5), categoryId, unitId, 10);
        when(productService.createProduct(request)).thenReturn(productResponse);

        ResponseEntity<ApiResponse<ProductResponse>> response = productController.createProduct(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(productResponse);
        verify(productService).createProduct(request);
    }

    @Test
    void updateProduct_returnsOkWithServiceResult() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Coke Zero", "SKU-002", "Sugar-free", BigDecimal.valueOf(1.75), categoryId, unitId);
        when(productService.updateProduct(productId, request)).thenReturn(productResponse);

        ResponseEntity<ApiResponse<ProductResponse>> response = productController.updateProduct(productId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(productResponse);
        verify(productService).updateProduct(productId, request);
    }

    @Test
    void deleteProduct_returnsOkAndDelegatesToService() {
        ResponseEntity<ApiResponse<Void>> response = productController.deleteProduct(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(productService).deleteProduct(productId);
    }
}
