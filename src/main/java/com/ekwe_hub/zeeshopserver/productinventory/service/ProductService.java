package com.ekwe_hub.zeeshopserver.productinventory.service;

import com.ekwe_hub.zeeshopserver.shared.api.exception.DuplicateResourceException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.CreateProductRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.UpdateProductRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.ProductResponse;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.productinventory.mapper.ProductMapper;
import com.ekwe_hub.zeeshopserver.productinventory.repository.CategoryRepository;
import com.ekwe_hub.zeeshopserver.productinventory.repository.InventoryRepository;
import com.ekwe_hub.zeeshopserver.productinventory.repository.ProductRepository;
import com.ekwe_hub.zeeshopserver.productinventory.repository.ProductSpecifications;
import com.ekwe_hub.zeeshopserver.productinventory.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * CRUD for the product catalogue. Every Product owns exactly one Inventory
 * record, so this service also provisions/retires that record alongside the
 * product it belongs to — the same way UserService resolves a Role when
 * creating a User. Adjusting stock quantities on an existing product is a
 * separate concern and does not live here.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductMapper productMapper;

    public PageResponse<ProductResponse> getAllProducts(String name, UUID categoryId, UUID unitId, Pageable pageable) {
        Page<Product> products = productRepository.findAll(
                ProductSpecifications.withFilters(name, categoryId, unitId), pageable);
        Page<ProductResponse> responses = products
                .map(product -> productMapper.toResponse(product, findInventoryOrThrow(product.getId())));
        return PageResponse.from(responses);
    }

    public ProductResponse getProduct(UUID id) {
        Product product = findProductOrThrow(id);
        return productMapper.toResponse(product, findInventoryOrThrow(id));
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product", "sku", request.sku());
        }

        Product product = productMapper.toEntity(
                request,
                findCategoryOrThrow(request.categoryId()),
                findUnitOrThrow(request.unitId())
        );
        product = productRepository.save(product);

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantityOnHand(request.initialQuantity() == null ? 0 : request.initialQuantity())
                .build();
        inventory = inventoryRepository.save(inventory);

        return productMapper.toResponse(product, inventory);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = findProductOrThrow(id);

        if (productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new DuplicateResourceException("Product", "sku", request.sku());
        }

        productMapper.updateEntity(
                request,
                findCategoryOrThrow(request.categoryId()),
                findUnitOrThrow(request.unitId()),
                product
        );

        product = productRepository.save(product);
        return productMapper.toResponse(product, findInventoryOrThrow(id));
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = findProductOrThrow(id);
        inventoryRepository.findByProductId(id).ifPresent(inventoryRepository::delete);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Category findCategoryOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private Unit findUnitOrThrow(UUID id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
    }

    private Inventory findInventoryOrThrow(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId));
    }
}
