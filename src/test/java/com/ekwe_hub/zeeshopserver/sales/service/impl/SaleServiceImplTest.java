package com.ekwe_hub.zeeshopserver.sales.service.impl;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.CategoryRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.InventoryRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.ProductRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.UnitRepository;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.InventoryService;
import com.ekwe_hub.zeeshopserver.sales.dto.request.CreateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.SaleItemRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.UpdateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.response.SaleResponse;
import com.ekwe_hub.zeeshopserver.sales.entity.PaymentType;
import com.ekwe_hub.zeeshopserver.sales.entity.Sale;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleStatus;
import com.ekwe_hub.zeeshopserver.sales.event.SaleCompletedEvent;
import com.ekwe_hub.zeeshopserver.sales.event.SaleCreatedEvent;
import com.ekwe_hub.zeeshopserver.sales.mapper.SaleMapper;
import com.ekwe_hub.zeeshopserver.sales.repository.interfaces.SaleRepository;
import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.shared.domain.event.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Product product;
    private Sale sale;
    private SaleResponse saleResponse;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().name("Electronics").build();
        Unit unit = Unit.builder().name("Piece").symbol("pc").build();

        product = Product.builder()
                .sku("PROD-001")
                .name("Laptop")
                .price(new BigDecimal("1000.00"))
                .category(category)
                .unit(unit)
                .build();
        product.setId(UUID.randomUUID());

        sale = Sale.builder()
                .referenceNumber("SALE-001")
                .status(SaleStatus.PENDING)
                .totalAmount(new BigDecimal("1000.00"))
                .items(new ArrayList<>())
                .build();
        sale.setId(UUID.randomUUID());

        saleResponse = new SaleResponse(
                sale.getId(),
                "SALE-001",
                SaleStatus.PENDING,
                PaymentType.CASH,
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                List.of(),
                null,
                null
        );
    }

    @Test
    void getAllSales_shouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        when(saleRepository.search(null, null, null, null, pageable)).thenReturn(new PageImpl<>(List.of(sale)));
        when(saleMapper.toResponse(sale)).thenReturn(saleResponse);

        PageResponse<SaleResponse> result = saleService.getAllSales(null, null, null, null, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).referenceNumber()).isEqualTo("SALE-001");
    }

    @Test
    void getSale_shouldReturnResponse() {
        when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
        when(saleMapper.toResponse(sale)).thenReturn(saleResponse);

        SaleResponse result = saleService.getSale(sale.getId());

        assertThat(result.id()).isEqualTo(sale.getId());
    }

    @Test
    void createSale_shouldCreateSaleAndPublishEvent() {
        SaleItemRequest itemReq = new SaleItemRequest(product.getId(), 2, new BigDecimal("500.00"));
        CreateSaleRequest createReq = new CreateSaleRequest("SALE-001", PaymentType.CASH, null, null, null, null, BigDecimal.ZERO, "Notes", List.of(itemReq));

        when(saleMapper.toEntity(createReq)).thenReturn(sale);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);
        when(saleMapper.toResponse(sale)).thenReturn(saleResponse);

        SaleResponse result = saleService.createSale(createReq);

        assertThat(result).isNotNull();
        verify(saleRepository).save(any(Sale.class));
        verify(domainEventPublisher).publish(any(SaleCreatedEvent.class));
    }

    @Test
    void createCreditSale_withoutCustomerDetails_shouldThrowException() {
        SaleItemRequest itemReq = new SaleItemRequest(product.getId(), 2, new BigDecimal("500.00"));
        CreateSaleRequest createReq = new CreateSaleRequest("SALE-001", PaymentType.CREDIT, null, null, null, null, BigDecimal.ZERO, "Notes", List.of(itemReq));

        assertThatThrownBy(() -> saleService.createSale(createReq))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Customer identification details");
    }

    @Test
    void completeSale_shouldDeductInventoryAndPublishEvent() {
        when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toResponse(sale)).thenReturn(saleResponse);

        SaleResponse result = saleService.completeSale(sale.getId());

        assertThat(result).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        verify(domainEventPublisher).publish(any(SaleCompletedEvent.class));
    }

    @Test
    void completeSale_whenAlreadyCompleted_shouldThrowException() {
        sale.setStatus(SaleStatus.COMPLETED);
        when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.completeSale(sale.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
