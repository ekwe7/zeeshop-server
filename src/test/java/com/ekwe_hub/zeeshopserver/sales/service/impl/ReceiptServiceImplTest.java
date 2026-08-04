package com.ekwe_hub.zeeshopserver.sales.service.impl;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.sales.entity.Sale;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleItem;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleStatus;
import com.ekwe_hub.zeeshopserver.sales.repository.interfaces.SaleRepository;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private ReceiptServiceImpl receiptService;

    private UUID saleId;
    private Sale sale;

    @BeforeEach
    void setUp() {
        saleId = UUID.randomUUID();

        ReflectionTestUtils.setField(receiptService, "shopName", "ZeeShop Store");
        ReflectionTestUtils.setField(receiptService, "shopAddress", "123 Main Street");
        ReflectionTestUtils.setField(receiptService, "shopPhone", "+1234567890");

        Product product = Product.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(50.00))
                .build();

        SaleItem item = SaleItem.builder()
                .product(product)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build();

        sale = Sale.builder()
                .referenceNumber("SALE-12345")
                .status(SaleStatus.COMPLETED)
                .totalAmount(BigDecimal.valueOf(100.00))
                .items(List.of(item))
                .build();
        sale.setId(saleId);
        sale.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void generateReceiptPdf_Success() {
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));

        byte[] pdfBytes = receiptService.generateReceiptPdf(saleId);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
    }

    @Test
    void generateReceiptPdf_NotFound_ThrowsException() {
        when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.generateReceiptPdf(saleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
