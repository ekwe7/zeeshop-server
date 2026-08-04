package com.ekwe_hub.zeeshopserver.customerdebt.event.listener;

import com.ekwe_hub.zeeshopserver.customerdebt.entity.CustomerDebt;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtStatus;
import com.ekwe_hub.zeeshopserver.customerdebt.event.DebtCreatedEvent;
import com.ekwe_hub.zeeshopserver.customerdebt.repository.CustomerDebtRepository;
import com.ekwe_hub.zeeshopserver.sales.entity.PaymentType;
import com.ekwe_hub.zeeshopserver.sales.entity.Sale;
import com.ekwe_hub.zeeshopserver.sales.event.SaleCreatedEvent;
import com.ekwe_hub.zeeshopserver.sales.repository.interfaces.SaleRepository;
import com.ekwe_hub.zeeshopserver.shared.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaleCreatedEventListener {

    private final SaleRepository saleRepository;
    private final CustomerDebtRepository customerDebtRepository;
    private final DomainEventPublisher domainEventPublisher;

    @EventListener
    @Transactional
    public void handleSaleCreatedEvent(SaleCreatedEvent event) {
        log.info("Handling SaleCreatedEvent: saleId={}, totalAmount={}", event.getSaleId(), event.getTotalAmount());

        Sale sale = saleRepository.findById(event.getSaleId()).orElse(null);
        if (sale == null) {
            log.warn("Sale not found for saleId: {}", event.getSaleId());
            return;
        }

        if (sale.getPaymentType() == PaymentType.CREDIT) {
            CustomerDebt debt = CustomerDebt.builder()
                    .sale(sale)
                    .customerName(sale.getCustomerName())
                    .customerPhone(sale.getCustomerPhone())
                    .customerEmail(sale.getCustomerEmail())
                    .amount(sale.getTotalAmount())
                    .dueDate(sale.getDueDate())
                    .status(DebtStatus.UNPAID)
                    .notes(sale.getNotes())
                    .build();

            debt = customerDebtRepository.save(debt);
            log.info("Customer debt created: debtId={} for saleId={}", debt.getId(), sale.getId());

            domainEventPublisher.publish(new DebtCreatedEvent(
                    debt.getId(),
                    sale.getId(),
                    debt.getCustomerName(),
                    debt.getAmount(),
                    debt.getDueDate()
            ));
        }
    }
}
