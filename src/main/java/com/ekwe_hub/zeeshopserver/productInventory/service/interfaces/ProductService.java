package com.ekwe_hub.zeeshopserver.productInventory.service.interfaces;

import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateProductRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateProductRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * CRUD for the product catalogue. Every Product owns exactly one Inventory
 * record, so the implementation also provisions/retires that record alongside
 * the product it belongs to — the same way UserService resolves a Role when
 * creating a User. Adjusting stock quantities on an existing product is a
 * separate concern and does not live here (see InventoryService).
 */
public interface ProductService {

    PageResponse<ProductResponse> getAllProducts(String name, UUID categoryId, UUID unitId, Pageable pageable);

    ProductResponse getProduct(UUID id);

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(UUID id, UpdateProductRequest request);

    void deleteProduct(UUID id);
}
