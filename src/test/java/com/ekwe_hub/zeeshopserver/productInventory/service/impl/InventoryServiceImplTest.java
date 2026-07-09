package com.ekwe_hub.zeeshopserver.productInventory.service.impl;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.productInventory.mapper.InventoryMapper;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private UUID productId;
    private Product product;
    private Inventory inventory;
    private InventoryResponse inventoryResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        Category category = Category.builder().name("Beverages").build();
        Unit unit = Unit.builder().name("Kilogram").symbol("kg").build();

        product = Product.builder()
                .name("Coke")
                .sku("SKU-001")
                .price(BigDecimal.valueOf(1.5))
                .category(category)
                .unit(unit)
                .build();
        product.setId(productId);

        inventory = Inventory.builder()
                .product(product)
                .quantityOnHand(10)
                .build();

        inventoryResponse = InventoryResponse.builder()
                .productId(productId)
                .productName("Coke")
                .sku("SKU-001")
                .quantityOnHand(10)
                .build();
    }

    @Test
    void getAllInventory_mapsEveryPersistedInventory() {
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory));
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        List<InventoryResponse> result = inventoryService.getAllInventory();

        assertThat(result).containsExactly(inventoryResponse);
    }

    @Test
    void getInventoryByProduct_returnsMappedResponse_whenInventoryExists() {
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        InventoryResponse result = inventoryService.getInventoryByProduct(productId);

        assertThat(result).isEqualTo(inventoryResponse);
    }

    @Test
    void getInventoryByProduct_throwsResourceNotFound_whenInventoryMissing() {
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryByProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void adjustStock_increasesQuantity_whenDeltaPositive() {
        AdjustInventoryRequest request = new AdjustInventoryRequest(5);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        inventoryService.adjustStock(productId, request);

        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantityOnHand()).isEqualTo(15);
    }

    @Test
    void adjustStock_decreasesQuantity_whenDeltaNegative() {
        AdjustInventoryRequest request = new AdjustInventoryRequest(-4);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        inventoryService.adjustStock(productId, request);

        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantityOnHand()).isEqualTo(6);
    }

    @Test
    void adjustStock_throwsBusinessRuleViolation_whenResultWouldGoNegative() {
        AdjustInventoryRequest request = new AdjustInventoryRequest(-11);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.adjustStock(productId, request))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(inventoryRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void adjustStock_throwsResourceNotFound_whenInventoryMissing() {
        AdjustInventoryRequest request = new AdjustInventoryRequest(5);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.adjustStock(productId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inventoryRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }
}
