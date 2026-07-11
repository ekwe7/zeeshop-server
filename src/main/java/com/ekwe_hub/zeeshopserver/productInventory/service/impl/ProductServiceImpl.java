package com.ekwe_hub.zeeshopserver.productInventory.service.impl;

import com.ekwe_hub.zeeshopserver.shared.api.exception.DuplicateResourceException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateProductRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateProductRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.ProductResponse;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.productInventory.event.ProductCreatedEvent;
import com.ekwe_hub.zeeshopserver.productInventory.event.ProductDeletedEvent;
import com.ekwe_hub.zeeshopserver.productInventory.event.ProductUpdatedEvent;
import com.ekwe_hub.zeeshopserver.productInventory.mapper.ProductMapper;
import com.ekwe_hub.zeeshopserver.productInventory.repository.impl.ProductSpecifications;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.CategoryRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.InventoryRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.ProductRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.UnitRepository;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.ProductService;
import com.ekwe_hub.zeeshopserver.shared.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductMapper productMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public PageResponse<ProductResponse> getAllProducts(String name, UUID categoryId, UUID unitId, Pageable pageable) {
        Page<Product> products = productRepository.findAll(ProductSpecifications.matching(name, categoryId, unitId), pageable);
        Page<ProductResponse> responses = products
                .map(product -> productMapper.toResponse(product, findInventoryOrThrow(product.getId())));
        return PageResponse.from(responses);
    }

    @Override
    public ProductResponse getProduct(UUID id) {
        Product product = findProductOrThrow(id);
        return productMapper.toResponse(product, findInventoryOrThrow(id));
    }

    @Override
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
                .lowStockThreshold(request.lowStockThreshold() == null ? 0 : request.lowStockThreshold())
                .build();
        inventory = inventoryRepository.save(inventory);

        domainEventPublisher.publish(new ProductCreatedEvent(product.getId(), product.getSku(), product.getName()));

        return productMapper.toResponse(product, inventory);
    }

    @Override
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

        domainEventPublisher.publish(new ProductUpdatedEvent(product.getId(), product.getSku(), product.getName()));

        return productMapper.toResponse(product, findInventoryOrThrow(id));
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        Product product = findProductOrThrow(id);
        inventoryRepository.findByProductId(id).ifPresent(inventoryRepository::delete);
        productRepository.delete(product);

        domainEventPublisher.publish(new ProductDeletedEvent(product.getId(), product.getSku()));
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
