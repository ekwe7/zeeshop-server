package com.ekwe_hub.zeeshopserver.productinventory.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.CreateCategoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.UpdateCategoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.CategoryResponse;
import com.ekwe_hub.zeeshopserver.productinventory.service.CategoryService;
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
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private UUID categoryId;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("Beverages")
                .description("Drinks")
                .build();
    }

    @Test
    void getAllCategories_returnsOkWithServiceResult() {
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryResponse));

        ResponseEntity<ApiResponse<List<CategoryResponse>>> response = categoryController.getAllCategories();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(categoryResponse);
    }

    @Test
    void getCategory_returnsOkWithServiceResult() {
        when(categoryService.getCategory(categoryId)).thenReturn(categoryResponse);

        ResponseEntity<ApiResponse<CategoryResponse>> response = categoryController.getCategory(categoryId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(categoryResponse);
    }

    @Test
    void createCategory_returnsCreatedWithServiceResult() {
        CreateCategoryRequest request = new CreateCategoryRequest("Beverages", "Drinks");
        when(categoryService.createCategory(request)).thenReturn(categoryResponse);

        ResponseEntity<ApiResponse<CategoryResponse>> response = categoryController.createCategory(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(categoryResponse);
        verify(categoryService).createCategory(request);
    }

    @Test
    void updateCategory_returnsOkWithServiceResult() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Snacks", "Chips and such");
        when(categoryService.updateCategory(categoryId, request)).thenReturn(categoryResponse);

        ResponseEntity<ApiResponse<CategoryResponse>> response = categoryController.updateCategory(categoryId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(categoryResponse);
        verify(categoryService).updateCategory(categoryId, request);
    }

    @Test
    void deleteCategory_returnsOkAndDelegatesToService() {
        ResponseEntity<ApiResponse<Void>> response = categoryController.deleteCategory(categoryId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(categoryService).deleteCategory(categoryId);
    }
}
