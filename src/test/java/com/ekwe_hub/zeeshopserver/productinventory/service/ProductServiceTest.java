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
import com.ekwe_hub.zeeshopserver.productinventory.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests: ProductRepository/CategoryRepository/UnitRepository/
 * InventoryRepository/ProductMapper are all mocked, no Spring context and no
 * real persistence involved.
 *
 * ProductService only orchestrates (validate -> resolve dependencies -> call
 * mapper/repository), so these tests verify orchestration and delegation,
 * not field-by-field conversion.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private UUID categoryId;
    private UUID unitId;
    private Category category;
    private Unit unit;
    private Product product;
    private Inventory inventory;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        unitId = UUID.randomUUID();

        category = Category.builder().name("Beverages").build();
        category.setId(categoryId);

        unit = Unit.builder().name("Kilogram").symbol("kg").build();
        unit.setId(unitId);

        product = Product.builder()
                .name("Coke")
                .sku("SKU-001")
                .description("Soft drink")
                .price(BigDecimal.valueOf(1.5))
                .category(category)
                .unit(unit)
                .build();
        product.setId(productId);

        inventory = Inventory.builder()
                .product(product)
                .quantityOnHand(10)
                .build();

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
    void getAllProducts_mapsEveryPersistedProduct() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(productMapper.toResponse(product, inventory)).thenReturn(productResponse);

        PageResponse<ProductResponse> result = productService.getAllProducts(null, null, null, pageable);

        assertThat(result.content()).containsExactly(productResponse);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void getProduct_returnsMappedResponse_whenProductExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(productMapper.toResponse(product, inventory)).thenReturn(productResponse);

        ProductResponse result = productService.getProduct(productId);

        assertThat(result).isEqualTo(productResponse);
    }

    @Test
    void getProduct_throwsResourceNotFound_whenProductMissing() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProduct_throwsResourceNotFound_whenInventoryMissing() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createProduct_resolvesCategoryAndUnit_andProvisionsInventory() {
        CreateProductRequest request = new CreateProductRequest(
                "Coke", "SKU-001", "Soft drink", BigDecimal.valueOf(1.5), categoryId, unitId, 10);

        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(productMapper.toEntity(request, category, unit)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(productMapper.toResponse(product, inventory)).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(request);

        assertThat(result).isEqualTo(productResponse);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        assertThat(inventoryCaptor.getValue().getProduct()).isEqualTo(product);
        assertThat(inventoryCaptor.getValue().getQuantityOnHand()).isEqualTo(10);
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_defaultsInitialQuantityToZero_whenOmitted() {
        CreateProductRequest request = new CreateProductRequest(
                "Coke", "SKU-001", "Soft drink", BigDecimal.valueOf(1.5), categoryId, unitId, null);

        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(productMapper.toEntity(request, category, unit)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(productMapper.toResponse(product, inventory)).thenReturn(productResponse);

        productService.createProduct(request);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        assertThat(inventoryCaptor.getValue().getQuantityOnHand()).isZero();
    }

    @Test
    void createProduct_throwsDuplicateResource_whenSkuTaken() {
        CreateProductRequest request = new CreateProductRequest(
                "Coke", "SKU-001", "Soft drink", BigDecimal.valueOf(1.5), categoryId, unitId, 10);
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(productRepository, never()).save(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createProduct_throwsResourceNotFound_whenCategoryMissing() {
        CreateProductRequest request = new CreateProductRequest(
                "Coke", "SKU-001", "Soft drink", BigDecimal.valueOf(1.5), categoryId, unitId, 10);
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_throwsResourceNotFound_whenUnitMissing() {
        CreateProductRequest request = new CreateProductRequest(
                "Coke", "SKU-001", "Soft drink", BigDecimal.valueOf(1.5), categoryId, unitId, 10);
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_delegatesFieldAssignmentToMapper() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Coke Zero", "SKU-002", "Sugar-free", BigDecimal.valueOf(1.75), categoryId, unitId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot("SKU-002", productId)).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(productRepository.save(product)).thenReturn(product);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(productMapper.toResponse(product, inventory)).thenReturn(productResponse);

        ProductResponse result = productService.updateProduct(productId, request);

        assertThat(result).isEqualTo(productResponse);
        verify(productMapper).updateEntity(request, category, unit, product);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_throwsResourceNotFound_whenProductMissing() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Coke Zero", "SKU-002", "Sugar-free", BigDecimal.valueOf(1.75), categoryId, unitId);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_throwsDuplicateResource_whenSkuTakenBySomeoneElse() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Coke Zero", "taken-sku", "Sugar-free", BigDecimal.valueOf(1.75), categoryId, unitId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot("taken-sku", productId)).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_throwsResourceNotFound_whenCategoryMissing() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Coke Zero", "SKU-002", "Sugar-free", BigDecimal.valueOf(1.75), categoryId, unitId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot("SKU-002", productId)).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_throwsResourceNotFound_whenUnitMissing() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Coke Zero", "SKU-002", "Sugar-free", BigDecimal.valueOf(1.75), categoryId, unitId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot("SKU-002", productId)).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_deletesProductAndInventory_whenProductExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        productService.deleteProduct(productId);

        verify(inventoryRepository, times(1)).delete(inventory);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_deletesProduct_whenInventoryAlreadyMissing() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        productService.deleteProduct(productId);

        verify(inventoryRepository, never()).delete(any());
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_throwsResourceNotFound_whenProductMissing() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any(Product.class));
        verify(inventoryRepository, never()).delete(any());
    }
}
