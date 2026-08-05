package com.ekwe_hub.zeeshopserver.customerdebt.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class DebtCreatedEvent extends AbstractDomainEvent {

    private final UUID debtId;
    private final UUID saleId;
    private final String customerName;
    private final BigDecimal amount;
    private final LocalDate dueDate;

    public DebtCreatedEvent(UUID debtId, UUID saleId, String customerName, BigDecimal amount, LocalDate dueDate) {
        super(debtId.toString());
        this.debtId = debtId;
        this.saleId = saleId;
        this.customerName = customerName;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    @Override
    public String getEventType() {
        return "customer_debt.created";
    }
}
