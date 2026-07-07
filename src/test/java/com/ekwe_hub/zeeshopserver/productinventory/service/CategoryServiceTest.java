package com.ekwe_hub.zeeshopserver.productinventory.service;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.DuplicateResourceException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.CreateCategoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.UpdateCategoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.CategoryResponse;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productinventory.mapper.CategoryMapper;
import com.ekwe_hub.zeeshopserver.productinventory.repository.CategoryRepository;
import com.ekwe_hub.zeeshopserver.productinventory.repository.ProductRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private UUID categoryId;
    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = Category.builder().name("Beverages").description("Drinks").build();
        category.setId(categoryId);

        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("Beverages")
                .description("Drinks")
                .build();
    }

    @Test
    void getAllCategories_mapsEveryPersistedCategory() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).containsExactly(categoryResponse);
    }

    @Test
    void getCategory_returnsMappedResponse_whenCategoryExists() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategory(categoryId);

        assertThat(result).isEqualTo(categoryResponse);
    }

    @Test
    void getCategory_throwsResourceNotFound_whenCategoryMissing() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCategory_savesAndReturnsMappedResponse() {
        CreateCategoryRequest request = new CreateCategoryRequest("Beverages", "Drinks");

        when(categoryRepository.existsByName("Beverages")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result).isEqualTo(categoryResponse);
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_throwsDuplicateResource_whenNameTaken() {
        CreateCategoryRequest request = new CreateCategoryRequest("Beverages", "Drinks");
        when(categoryRepository.existsByName("Beverages")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_delegatesFieldAssignmentToMapper() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Snacks", "Chips and such");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndIdNot("Snacks", categoryId)).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.updateCategory(categoryId, request);

        assertThat(result).isEqualTo(categoryResponse);
        verify(categoryMapper).updateEntity(request, category);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_throwsResourceNotFound_whenCategoryMissing() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Snacks", "Chips and such");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_throwsDuplicateResource_whenNameTakenBySomeoneElse() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("taken-name", "Chips and such");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndIdNot("taken-name", categoryId)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_deletesCategory_whenNotReferencedByAnyProduct() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(categoryId)).thenReturn(false);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_throwsBusinessRuleViolation_whenReferencedByProduct() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(categoryId)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_throwsResourceNotFound_whenCategoryMissing() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).delete(any());
    }
}
