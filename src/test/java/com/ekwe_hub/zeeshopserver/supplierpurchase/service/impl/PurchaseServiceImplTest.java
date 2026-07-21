package com.ekwe_hub.zeeshopserver.supplierpurchase.service.impl;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.ProductRepository;
import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PurchaseItemRepository purchaseItemRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseMapper purchaseMapper;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private UUID purchaseId;
    private UUID supplierId;
    private UUID productId;
    private Supplier supplier;
    private Product product;
    private Purchase purchase;
    private PurchaseItem purchaseItem;
    private PurchaseResponse purchaseResponse;

    @BeforeEach
    void setUp() {
        purchaseId = UUID.randomUUID();
        supplierId = UUID.randomUUID();
        productId = UUID.randomUUID();

        supplier = Supplier.builder().name("Acme Distributors").balance(BigDecimal.ZERO).build();
        supplier.setId(supplierId);

        product = Product.builder().name("Widget").sku("WID-1").price(new BigDecimal("5.00")).build();
        product.setId(productId);

        purchaseItem = PurchaseItem.builder()
                .product(product)
                .quantityOrdered(10)
                .quantityReceived(0)
                .unitCost(new BigDecimal("2.00"))
                .build();
        purchaseItem.setId(UUID.randomUUID());

        purchase = Purchase.builder()
                .supplier(supplier)
                .status(PurchaseStatus.PENDING)
                .totalAmount(new BigDecimal("20.00"))
                .build();
        purchase.setId(purchaseId);
        purchase.getItems().add(purchaseItem);
        purchaseItem.setPurchase(purchase);

        purchaseResponse = PurchaseResponse.builder()
                .id(purchaseId)
                .supplierId(supplierId)
                .status(PurchaseStatus.PENDING)
                .totalAmount(new BigDecimal("20.00"))
                .build();
    }

    @Test
    void getAllPurchases_mapsSearchResults() {
        Pageable pageable = PageRequest.of(0, 20);
        when(purchaseRepository.search(supplierId, PurchaseStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(purchase)));
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        var result = purchaseService.getAllPurchases(supplierId, PurchaseStatus.PENDING, pageable);

        assertThat(result.content()).containsExactly(purchaseResponse);
    }

    @Test
    void getPurchase_throwsResourceNotFound_whenMissing() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.getPurchase(purchaseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createPurchase_resolvesSupplierAndProducts_computesTotalAndPublishesEvent() {
        PurchaseItemRequest itemRequest = new PurchaseItemRequest(productId, 10, new BigDecimal("2.00"));
        CreatePurchaseRequest request = new CreatePurchaseRequest(supplierId, "INV-1", null, List.of(itemRequest));

        Purchase newPurchase = Purchase.builder().supplier(supplier).status(PurchaseStatus.PENDING).build();
        newPurchase.setId(UUID.randomUUID());

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(purchaseMapper.toEntity(request, supplier)).thenReturn(newPurchase);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseMapper.toItemEntity(itemRequest, product, newPurchase)).thenReturn(purchaseItem);
        when(purchaseRepository.save(newPurchase)).thenReturn(newPurchase);
        when(purchaseMapper.toResponse(newPurchase)).thenReturn(purchaseResponse);

        PurchaseResponse result = purchaseService.createPurchase(request);

        assertThat(result).isEqualTo(purchaseResponse);
        assertThat(newPurchase.getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(newPurchase.getItems()).containsExactly(purchaseItem);
        verify(domainEventPublisher).publish(any(PurchaseCreatedEvent.class));
    }

    @Test
    void createPurchase_throwsResourceNotFound_whenSupplierMissing() {
        CreatePurchaseRequest request = new CreatePurchaseRequest(supplierId, null, null,
                List.of(new PurchaseItemRequest(productId, 10, new BigDecimal("2.00"))));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.createPurchase(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void createPurchase_throwsResourceNotFound_whenProductMissing() {
        CreatePurchaseRequest request = new CreatePurchaseRequest(supplierId, null, null,
                List.of(new PurchaseItemRequest(productId, 10, new BigDecimal("2.00"))));
        Purchase newPurchase = Purchase.builder().supplier(supplier).build();

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(purchaseMapper.toEntity(request, supplier)).thenReturn(newPurchase);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.createPurchase(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void updatePurchase_delegatesFieldAssignmentToMapper_whenPending() {
        UpdatePurchaseRequest request = new UpdatePurchaseRequest("INV-2", "updated notes");
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        PurchaseResponse result = purchaseService.updatePurchase(purchaseId, request);

        assertThat(result).isEqualTo(purchaseResponse);
        verify(purchaseMapper).updateEntity(request, purchase);
    }

    @Test
    void updatePurchase_throwsBusinessRuleViolation_whenCompleted() {
        purchase.setStatus(PurchaseStatus.COMPLETED);
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> purchaseService.updatePurchase(purchaseId, new UpdatePurchaseRequest("x", "y")))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void deletePurchase_deletesPurchase_whenPending() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

        purchaseService.deletePurchase(purchaseId);

        verify(purchaseRepository).delete(purchase);
    }

    @Test
    void deletePurchase_throwsBusinessRuleViolation_whenPartiallyReceived() {
        purchase.setStatus(PurchaseStatus.PARTIALLY_RECEIVED);
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> purchaseService.deletePurchase(purchaseId))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(purchaseRepository, never()).delete(any());
    }

    @Test
    void receiveStock_partialReceipt_setsPartiallyReceivedAndAdjustsInventory() {
        ReceiveStockRequest request = new ReceiveStockRequest(List.of(new ReceiveItemRequest(purchaseItem.getId(), 4)));

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseItemRepository.findByIdAndPurchaseId(purchaseItem.getId(), purchaseId))
                .thenReturn(Optional.of(purchaseItem));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        purchaseService.receiveStock(purchaseId, request);

        assertThat(purchaseItem.getQuantityReceived()).isEqualTo(4);
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.PARTIALLY_RECEIVED);
        verify(domainEventPublisher).publish(any(StockReceivedEvent.class));
        verify(domainEventPublisher, never()).publish(any(PurchaseCompletedEvent.class));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void receiveStock_fullReceipt_completesPurchaseAndIncreasesSupplierBalance() {
        ReceiveStockRequest request = new ReceiveStockRequest(List.of(new ReceiveItemRequest(purchaseItem.getId(), 10)));

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseItemRepository.findByIdAndPurchaseId(purchaseItem.getId(), purchaseId))
                .thenReturn(Optional.of(purchaseItem));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        purchaseService.receiveStock(purchaseId, request);

        assertThat(purchaseItem.getQuantityReceived()).isEqualTo(10);
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.COMPLETED);
        assertThat(supplier.getBalance()).isEqualByComparingTo("20.00");
        verify(supplierRepository).save(supplier);
        verify(domainEventPublisher).publish(any(StockReceivedEvent.class));
        verify(domainEventPublisher).publish(any(PurchaseCompletedEvent.class));
    }

    @Test
    void receiveStock_throwsBusinessRuleViolation_whenReceivingMoreThanOrdered() {
        ReceiveStockRequest request = new ReceiveStockRequest(List.of(new ReceiveItemRequest(purchaseItem.getId(), 11)));

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseItemRepository.findByIdAndPurchaseId(purchaseItem.getId(), purchaseId))
                .thenReturn(Optional.of(purchaseItem));

        assertThatThrownBy(() -> purchaseService.receiveStock(purchaseId, request))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void receiveStock_throwsBusinessRuleViolation_whenPurchaseAlreadyCompleted() {
        purchase.setStatus(PurchaseStatus.COMPLETED);
        ReceiveStockRequest request = new ReceiveStockRequest(List.of(new ReceiveItemRequest(purchaseItem.getId(), 1)));
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> purchaseService.receiveStock(purchaseId, request))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(purchaseItemRepository, never()).findByIdAndPurchaseId(any(), any());
    }

    @Test
    void receiveStock_throwsResourceNotFound_whenPurchaseItemDoesNotBelongToPurchase() {
        UUID unknownItemId = UUID.randomUUID();
        ReceiveStockRequest request = new ReceiveStockRequest(List.of(new ReceiveItemRequest(unknownItemId, 1)));

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseItemRepository.findByIdAndPurchaseId(unknownItemId, purchaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.receiveStock(purchaseId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelPurchase_cancelsPurchase_whenPending() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        purchaseService.cancelPurchase(purchaseId);

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.CANCELLED);
    }

    @Test
    void cancelPurchase_throwsBusinessRuleViolation_whenNotPending() {
        purchase.setStatus(PurchaseStatus.PARTIALLY_RECEIVED);
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> purchaseService.cancelPurchase(purchaseId))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(purchaseRepository, never()).save(any());
    }
}
