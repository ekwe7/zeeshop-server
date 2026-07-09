package com.ekwe_hub.zeeshopserver.productInventory.service.interfaces;

import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateCategoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateCategoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * CRUD for product categories. Deletion is blocked while any Product still
 * references the category — Product.category is a required (not-null)
 * relationship, so an unguarded delete would either fail on the database FK
 * constraint or orphan products, depending on cascade settings.
 */
public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategory(UUID id);

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);

    void deleteCategory(UUID id);
}
