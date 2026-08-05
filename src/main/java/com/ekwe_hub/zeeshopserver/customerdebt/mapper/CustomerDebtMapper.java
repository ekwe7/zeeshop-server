package com.ekwe_hub.zeeshopserver.customerdebt.mapper;

import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.CreateCustomerRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.UpdateCustomerRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerDebtResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.DebtPaymentResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.Customer;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.CustomerDebt;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtPayment;
import org.springframework.stereotype.Component;

@Component
public class CustomerDebtMapper {

    public Customer toEntity(CreateCustomerRequest request) {
        return Customer.builder()
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .build();
    }

    public void updateEntity(UpdateCustomerRequest request, Customer customer) {
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getTotalDebt(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public CustomerDebtResponse toDebtResponse(CustomerDebt debt) {
        return new CustomerDebtResponse(
                debt.getId(),
                debt.getCustomer() != null ? debt.getCustomer().getId() : null,
                debt.getSale() != null ? debt.getSale().getId() : null,
                debt.getCustomerName(),
                debt.getCustomerPhone(),
                debt.getCustomerEmail(),
                debt.getInitialAmount(),
                debt.getAmount(),
                debt.getPaidAmount(),
                debt.getDueDate(),
                debt.getStatus(),
                debt.getNotes(),
                debt.getCreatedAt(),
                debt.getUpdatedAt()
        );
    }

    public DebtPaymentResponse toPaymentResponse(DebtPayment payment) {
        return new DebtPaymentResponse(
                payment.getId(),
                payment.getDebt() != null ? payment.getDebt().getId() : null,
                payment.getAmount(),
                payment.getPaymentType(),
                payment.getReferenceNumber(),
                payment.getNotes(),
                payment.getCreatedAt()
        );
    }
}
