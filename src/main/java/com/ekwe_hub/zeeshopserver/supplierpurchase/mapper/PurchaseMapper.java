package com.ekwe_hub.zeeshopserver.supplierpurchase.mapper;

import com.ekwe_hub.zeeshopserver.productinventory.entity.Product;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.PurchaseItemRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.PurchaseItemResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.PurchaseResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Purchase;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseItem;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Supplier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PurchaseMapper {

    public Purchase toEntity(CreatePurchaseRequest request, Supplier supplier) {
        return Purchase.builder()
                .supplier(supplier)
                .invoiceNumber(request.invoiceNumber())
                .notes(request.notes())
                .build();
    }

    public PurchaseItem toItemEntity(PurchaseItemRequest request, Product product, Purchase purchase) {
        return PurchaseItem.builder()
                .purchase(purchase)
                .product(product)
                .quantityOrdered(request.quantityOrdered())
                .unitCost(request.unitCost())
                .build();
    }

    public void updateEntity(UpdatePurchaseRequest request, Purchase purchase) {
        purchase.setInvoiceNumber(request.invoiceNumber());
        purchase.setNotes(request.notes());
    }

    public PurchaseResponse toResponse(Purchase purchase) {
        return PurchaseResponse.builder()
                .id(purchase.getId())
                .supplierId(purchase.getSupplier().getId())
                .supplierName(purchase.getSupplier().getName())
                .status(purchase.getStatus())
                .invoiceNumber(purchase.getInvoiceNumber())
                .notes(purchase.getNotes())
                .totalAmount(purchase.getTotalAmount())
                .items(purchase.getItems().stream().map(this::toItemResponse).toList())
                .createdAt(purchase.getCreatedAt())
                .updatedAt(purchase.getUpdatedAt())
                .build();
    }

    public PurchaseItemResponse toItemResponse(PurchaseItem item) {
        return PurchaseItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantityOrdered(item.getQuantityOrdered())
                .quantityReceived(item.getQuantityReceived())
                .unitCost(item.getUnitCost())
                .lineTotal(item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantityOrdered())))
                .build();
    }
}
