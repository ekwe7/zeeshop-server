package com.ekwe_hub.zeeshopserver.sales.mapper;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.sales.dto.request.CreateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.SaleItemRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.UpdateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.response.SaleItemResponse;
import com.ekwe_hub.zeeshopserver.sales.dto.response.SaleResponse;
import com.ekwe_hub.zeeshopserver.sales.entity.Sale;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SaleMapper {

    public Sale toEntity(CreateSaleRequest request) {
        return Sale.builder()
                .referenceNumber(request.referenceNumber())
                .notes(request.notes())
                .build();
    }

    public SaleItem toItemEntity(SaleItemRequest request, Product product, Sale sale) {
        return SaleItem.builder()
                .sale(sale)
                .product(product)
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .build();
    }

    public void updateEntity(UpdateSaleRequest request, Sale sale) {
        if (request.referenceNumber() != null) {
            sale.setReferenceNumber(request.referenceNumber());
        }
        if (request.notes() != null) {
            sale.setNotes(request.notes());
        }
    }

    public SaleResponse toResponse(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getReferenceNumber(),
                sale.getStatus(),
                sale.getNotes(),
                sale.getTotalAmount(),
                sale.getItems().stream().map(this::toItemResponse).toList(),
                sale.getCreatedAt(),
                sale.getUpdatedAt()
        );
    }

    public SaleItemResponse toItemResponse(SaleItem item) {
        BigDecimal subtotal = item.getUnitPrice() != null 
                ? item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                : BigDecimal.ZERO;

        return new SaleItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal
        );
    }
}
