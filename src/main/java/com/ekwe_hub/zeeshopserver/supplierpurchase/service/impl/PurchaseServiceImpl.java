package com.ekwe_hub.zeeshopserver.supplierpurchase.service.impl;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.ProductRepository;
import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.shared.domain.event.DomainEventPublisher;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.PurchaseItemRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.ReceiveItemRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.ReceiveStockRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.PurchaseResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Purchase;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseItem;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseStatus;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Supplier;
import com.ekwe_hub.zeeshopserver.supplierpurchase.event.PurchaseCompletedEvent;
import com.ekwe_hub.zeeshopserver.supplierpurchase.event.PurchaseCreatedEvent;
import com.ekwe_hub.zeeshopserver.supplierpurchase.event.StockReceivedEvent;
import com.ekwe_hub.zeeshopserver.supplierpurchase.mapper.PurchaseMapper;
import com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces.PurchaseItemRepository;
import com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces.PurchaseRepository;
import com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces.SupplierRepository;
import com.ekwe_hub.zeeshopserver.supplierpurchase.service.interfaces.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseMapper purchaseMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public PageResponse<PurchaseResponse> getAllPurchases(UUID supplierId, PurchaseStatus status, Pageable pageable) {
        Page<PurchaseResponse> responses = purchaseRepository.search(supplierId, status, pageable)
                .map(purchaseMapper::toResponse);
        return PageResponse.from(responses);
    }

    @Override
    public PurchaseResponse getPurchase(UUID id) {
        return purchaseMapper.toResponse(findPurchaseOrThrow(id));
    }

    @Override
    @Transactional
    public PurchaseResponse createPurchase(CreatePurchaseRequest request) {
        Supplier supplier = findSupplierOrThrow(request.supplierId());

        Purchase purchase = purchaseMapper.toEntity(request, supplier);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseItemRequest itemRequest : request.items()) {
            Product product = findProductOrThrow(itemRequest.productId());
            PurchaseItem item = purchaseMapper.toItemEntity(itemRequest, product, purchase);
            purchase.getItems().add(item);
            totalAmount = totalAmount.add(itemRequest.unitCost().multiply(BigDecimal.valueOf(itemRequest.quantityOrdered())));
        }
        purchase.setTotalAmount(totalAmount);

        purchase = purchaseRepository.save(purchase);

        domainEventPublisher.publish(new PurchaseCreatedEvent(purchase.getId(), supplier.getId(), totalAmount));

        return purchaseMapper.toResponse(purchase);
    }

    @Override
    @Transactional
    public PurchaseResponse updatePurchase(UUID id, UpdatePurchaseRequest request) {
        Purchase purchase = findPurchaseOrThrow(id);

        if (purchase.getStatus() == PurchaseStatus.COMPLETED || purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new BusinessRuleViolationException(
                    "Cannot update a purchase that is %s".formatted(purchase.getStatus().name().toLowerCase()));
        }

        purchaseMapper.updateEntity(request, purchase);
        purchase = purchaseRepository.save(purchase);
        return purchaseMapper.toResponse(purchase);
    }

    @Override
    @Transactional
    public void deletePurchase(UUID id) {
        Purchase purchase = findPurchaseOrThrow(id);

        if (purchase.getStatus() == PurchaseStatus.PARTIALLY_RECEIVED || purchase.getStatus() == PurchaseStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "Cannot delete a purchase once stock has been received against it");
        }

        purchaseRepository.delete(purchase);
    }

    @Override
    @Transactional
    public PurchaseResponse receiveStock(UUID id, ReceiveStockRequest request) {
        Purchase purchase = findPurchaseOrThrow(id);

        if (purchase.getStatus() == PurchaseStatus.COMPLETED || purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new BusinessRuleViolationException(
                    "Cannot receive stock for a purchase that is %s".formatted(purchase.getStatus().name().toLowerCase()));
        }

        for (ReceiveItemRequest line : request.items()) {
            PurchaseItem item = purchaseItemRepository.findByIdAndPurchaseId(line.purchaseItemId(), id)
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase item", line.purchaseItemId()));

            int newReceived = item.getQuantityReceived() + line.quantity();
            if (newReceived > item.getQuantityOrdered()) {
                throw new BusinessRuleViolationException(
                        "Cannot receive %d of '%s': %d already received against an order of %d"
                                .formatted(line.quantity(), item.getProduct().getName(),
                                        item.getQuantityReceived(), item.getQuantityOrdered()));
            }

            item.setQuantityReceived(newReceived);
            purchaseItemRepository.save(item);

            domainEventPublisher.publish(new StockReceivedEvent(purchase.getId(), item.getProduct().getId(), line.quantity()));
        }

        boolean allReceived = purchase.getItems().stream()
                .allMatch(item -> item.getQuantityReceived() >= item.getQuantityOrdered());
        boolean anyReceived = purchase.getItems().stream()
                .anyMatch(item -> item.getQuantityReceived() > 0);

        if (allReceived) {
            purchase.setStatus(PurchaseStatus.COMPLETED);

            Supplier supplier = purchase.getSupplier();
            supplier.setBalance(supplier.getBalance().add(purchase.getTotalAmount()));
            supplierRepository.save(supplier);

            domainEventPublisher.publish(
                    new PurchaseCompletedEvent(purchase.getId(), supplier.getId(), purchase.getTotalAmount()));
        } else if (anyReceived) {
            purchase.setStatus(PurchaseStatus.PARTIALLY_RECEIVED);
        }

        purchase = purchaseRepository.save(purchase);
        return purchaseMapper.toResponse(purchase);
    }

    @Override
    @Transactional
    public PurchaseResponse cancelPurchase(UUID id) {
        Purchase purchase = findPurchaseOrThrow(id);

        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new BusinessRuleViolationException(
                    "Only a pending purchase with no stock received can be cancelled");
        }

        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase = purchaseRepository.save(purchase);
        return purchaseMapper.toResponse(purchase);
    }

    private Purchase findPurchaseOrThrow(UUID id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", id));
    }

    private Supplier findSupplierOrThrow(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
