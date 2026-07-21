package com.ekwe_hub.zeeshopserver.productInventory.event.listener;

import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.InventoryService;
import com.ekwe_hub.zeeshopserver.supplierpurchase.event.StockReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReceivedEventListener {

    private final InventoryService inventoryService;

    @EventListener
    public void handleStockReceivedEvent(StockReceivedEvent event) {
        log.info("Handling StockReceivedEvent: purchaseId={}, productId={}, quantity={}",
                event.getPurchaseId(), event.getProductId(), event.getQuantityReceived());

        inventoryService.adjustStock(
                event.getProductId(),
                new AdjustInventoryRequest(
                        event.getQuantityReceived(),
                        "Stock received from purchase order: " + event.getPurchaseId()
                )
        );
    }
}
