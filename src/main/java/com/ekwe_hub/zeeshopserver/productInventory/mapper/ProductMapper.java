package com.ekwe_hub.zeeshopserver.productInventory.mapper;

import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateProductRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateProductRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.ProductResponse;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request, Category category, Unit unit) {
        return Product.builder()
                .name(request.name())
                .sku(request.sku())
                .description(request.description())
                .price(request.price())
                .category(category)
                .unit(unit)
                .build();
    }

    public void updateEntity(UpdateProductRequest request, Category category, Unit unit, Product product) {
        product.setName(request.name());
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setUnit(unit);
    }

    public ProductResponse toResponse(Product product, Inventory inventory) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryName(product.getCategory().getName())
                .unitName(product.getUnit().getName())
                .quantityOnHand(inventory.getQuantityOnHand())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
