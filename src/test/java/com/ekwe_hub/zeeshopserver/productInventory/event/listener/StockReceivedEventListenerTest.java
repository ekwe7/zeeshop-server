package com.ekwe_hub.zeeshopserver.productInventory.event.listener;

import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.InventoryService;
import com.ekwe_hub.zeeshopserver.supplierpurchase.event.StockReceivedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockReceivedEventListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private StockReceivedEventListener listener;

    @Test
    void handleStockReceivedEvent_triggersAdjustStock() {
        UUID purchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockReceivedEvent event = new StockReceivedEvent(purchaseId, productId, 7);

        listener.handleStockReceivedEvent(event);

        ArgumentCaptor<AdjustInventoryRequest> captor = ArgumentCaptor.forClass(AdjustInventoryRequest.class);
        verify(inventoryService).adjustStock(eq(productId), captor.capture());
        
        assertThat(captor.getValue().quantity()).isEqualTo(7);
        assertThat(captor.getValue().reason()).isEqualTo("Stock received from purchase order: " + purchaseId);
    }
}
