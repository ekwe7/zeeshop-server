package com.ekwe_hub.zeeshopserver.productinventory.mapper;

import com.ekwe_hub.zeeshopserver.productinventory.dto.request.CreateCategoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.UpdateCategoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.CategoryResponse;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request) {
        return Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    public void updateEntity(UpdateCategoryRequest request, Category category) {
        category.setName(request.name());
        category.setDescription(request.description());
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
