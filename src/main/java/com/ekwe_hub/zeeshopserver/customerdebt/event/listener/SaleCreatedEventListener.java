package com.ekwe_hub.zeeshopserver.customerdebt.event.listener;

import com.ekwe_hub.zeeshopserver.customerdebt.entity.Customer;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.CustomerDebt;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtStatus;
import com.ekwe_hub.zeeshopserver.customerdebt.event.DebtCreatedEvent;
import com.ekwe_hub.zeeshopserver.customerdebt.repository.CustomerDebtRepository;
import com.ekwe_hub.zeeshopserver.customerdebt.repository.CustomerRepository;
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

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaleCreatedEventListener {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
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
            Customer customer = findOrCreateCustomer(sale);

            CustomerDebt debt = CustomerDebt.builder()
                    .customer(customer)
                    .sale(sale)
                    .customerName(sale.getCustomerName() != null ? sale.getCustomerName() : (customer != null ? customer.getName() : "Unknown"))
                    .customerPhone(sale.getCustomerPhone() != null ? sale.getCustomerPhone() : (customer != null ? customer.getPhone() : null))
                    .customerEmail(sale.getCustomerEmail() != null ? sale.getCustomerEmail() : (customer != null ? customer.getEmail() : null))
                    .initialAmount(sale.getTotalAmount())
                    .amount(sale.getTotalAmount())
                    .paidAmount(BigDecimal.ZERO)
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

    private Customer findOrCreateCustomer(Sale sale) {
        if (sale.getCustomerPhone() != null && !sale.getCustomerPhone().isBlank()) {
            Optional<Customer> existing = customerRepository.findByPhone(sale.getCustomerPhone());
            if (existing.isPresent()) {
                Customer c = existing.get();
                c.setTotalDebt(c.getTotalDebt().add(sale.getTotalAmount()));
                return customerRepository.save(c);
            }
        }

        if (sale.getCustomerEmail() != null && !sale.getCustomerEmail().isBlank()) {
            Optional<Customer> existing = customerRepository.findByEmail(sale.getCustomerEmail());
            if (existing.isPresent()) {
                Customer c = existing.get();
                c.setTotalDebt(c.getTotalDebt().add(sale.getTotalAmount()));
                return customerRepository.save(c);
            }
        }

        if ((sale.getCustomerName() != null && !sale.getCustomerName().isBlank()) ||
            (sale.getCustomerPhone() != null && !sale.getCustomerPhone().isBlank()) ||
            (sale.getCustomerEmail() != null && !sale.getCustomerEmail().isBlank())) {

            Customer newCustomer = Customer.builder()
                    .name(sale.getCustomerName() != null ? sale.getCustomerName() : "Customer")
                    .phone(sale.getCustomerPhone())
                    .email(sale.getCustomerEmail())
                    .totalDebt(sale.getTotalAmount())
                    .build();

            return customerRepository.save(newCustomer);
        }

        return null;
    }
}
