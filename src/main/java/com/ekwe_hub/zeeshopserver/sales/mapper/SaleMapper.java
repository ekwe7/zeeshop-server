package com.ekwe_hub.zeeshopserver.sales.mapper;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.sales.dto.request.CreateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.SaleItemRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.UpdateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.response.SaleItemResponse;
import com.ekwe_hub.zeeshopserver.sales.dto.response.SaleResponse;
import com.ekwe_hub.zeeshopserver.sales.entity.PaymentType;
import com.ekwe_hub.zeeshopserver.sales.entity.Sale;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SaleMapper {

    public Sale toEntity(CreateSaleRequest request) {
        return Sale.builder()
                .referenceNumber(request.referenceNumber())
                .paymentType(request.paymentType() != null ? request.paymentType() : PaymentType.CASH)
                .customerName(request.customerName())
                .customerPhone(request.customerPhone())
                .customerEmail(request.customerEmail())
                .dueDate(request.dueDate())
                .discountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO)
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
        if (request.paymentType() != null) {
            sale.setPaymentType(request.paymentType());
        }
        if (request.customerName() != null) {
            sale.setCustomerName(request.customerName());
        }
        if (request.customerPhone() != null) {
            sale.setCustomerPhone(request.customerPhone());
        }
        if (request.customerEmail() != null) {
            sale.setCustomerEmail(request.customerEmail());
        }
        if (request.dueDate() != null) {
            sale.setDueDate(request.dueDate());
        }
        if (request.discountAmount() != null) {
            sale.setDiscountAmount(request.discountAmount());
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
                sale.getPaymentType(),
                sale.getCustomerName(),
                sale.getCustomerPhone(),
                sale.getCustomerEmail(),
                sale.getDueDate(),
                sale.getNotes(),
                sale.getSubtotalAmount(),
                sale.getDiscountAmount(),
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
