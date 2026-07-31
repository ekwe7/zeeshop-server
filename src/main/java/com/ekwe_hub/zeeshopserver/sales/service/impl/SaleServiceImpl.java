package com.ekwe_hub.zeeshopserver.sales.service.impl;

import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.ProductRepository;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.InventoryService;
import com.ekwe_hub.zeeshopserver.sales.dto.request.CreateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.SaleItemRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.UpdateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.response.SaleResponse;
import com.ekwe_hub.zeeshopserver.sales.entity.Sale;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleItem;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleStatus;
import com.ekwe_hub.zeeshopserver.sales.event.SaleCompletedEvent;
import com.ekwe_hub.zeeshopserver.sales.event.SaleCreatedEvent;
import com.ekwe_hub.zeeshopserver.sales.mapper.SaleMapper;
import com.ekwe_hub.zeeshopserver.sales.repository.interfaces.SaleRepository;
import com.ekwe_hub.zeeshopserver.sales.service.interfaces.SaleService;
import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.shared.domain.event.DomainEventPublisher;
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
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final SaleMapper saleMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public PageResponse<SaleResponse> getAllSales(SaleStatus status, Pageable pageable) {
        Page<SaleResponse> responses = saleRepository.search(status, pageable)
                .map(saleMapper::toResponse);
        return PageResponse.from(responses);
    }

    @Override
    public SaleResponse getSale(UUID id) {
        return saleMapper.toResponse(findSaleOrThrow(id));
    }

    @Override
    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {
        Sale sale = saleMapper.toEntity(request);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleItemRequest itemRequest : request.items()) {
            Product product = findProductOrThrow(itemRequest.productId());
            SaleItem item = saleMapper.toItemEntity(itemRequest, product, sale);
            sale.getItems().add(item);
            totalAmount = totalAmount.add(itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }
        sale.setTotalAmount(totalAmount);

        sale = saleRepository.save(sale);

        domainEventPublisher.publish(new SaleCreatedEvent(sale.getId(), totalAmount));

        return saleMapper.toResponse(sale);
    }

    @Override
    @Transactional
    public SaleResponse updateSale(UUID id, UpdateSaleRequest request) {
        Sale sale = findSaleOrThrow(id);

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new BusinessRuleViolationException(
                    "Cannot update a sale that is %s".formatted(sale.getStatus().name().toLowerCase()));
        }

        saleMapper.updateEntity(request, sale);
        sale = saleRepository.save(sale);
        return saleMapper.toResponse(sale);
    }

    @Override
    @Transactional
    public void deleteSale(UUID id) {
        Sale sale = findSaleOrThrow(id);

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Cannot delete a completed sale");
        }

        saleRepository.delete(sale);
    }

    @Override
    @Transactional
    public SaleResponse completeSale(UUID id) {
        Sale sale = findSaleOrThrow(id);

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new BusinessRuleViolationException(
                    "Cannot complete a sale that is %s".formatted(sale.getStatus().name().toLowerCase()));
        }

        // Deduct inventory stock for each sale item
        for (SaleItem item : sale.getItems()) {
            inventoryService.adjustStock(
                    item.getProduct().getId(),
                    new AdjustInventoryRequest(
                            -item.getQuantity(),
                            "Stock deducted for sale reference: " + sale.getReferenceNumber()
                    )
            );
        }

        sale.setStatus(SaleStatus.COMPLETED);
        sale = saleRepository.save(sale);

        domainEventPublisher.publish(new SaleCompletedEvent(sale.getId(), sale.getTotalAmount()));

        return saleMapper.toResponse(sale);
    }

    @Override
    @Transactional
    public SaleResponse cancelSale(UUID id) {
        Sale sale = findSaleOrThrow(id);

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new BusinessRuleViolationException(
                    "Only a pending sale can be cancelled");
        }

        sale.setStatus(SaleStatus.CANCELLED);
        sale = saleRepository.save(sale);
        return saleMapper.toResponse(sale);
    }

    private Sale findSaleOrThrow(UUID id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", id));
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
