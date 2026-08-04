package com.ekwe_hub.zeeshopserver.customerdebt.service.impl;

import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.CreateCustomerRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.RecordDebtPaymentRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.UpdateCustomerRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerDebtResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.DebtPaymentResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.Customer;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.CustomerDebt;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtPayment;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtStatus;
import com.ekwe_hub.zeeshopserver.customerdebt.mapper.CustomerDebtMapper;
import com.ekwe_hub.zeeshopserver.customerdebt.repository.CustomerDebtRepository;
import com.ekwe_hub.zeeshopserver.customerdebt.repository.CustomerRepository;
import com.ekwe_hub.zeeshopserver.customerdebt.repository.DebtPaymentRepository;
import com.ekwe_hub.zeeshopserver.customerdebt.service.interfaces.CustomerDebtService;
import com.ekwe_hub.zeeshopserver.sales.entity.PaymentType;
import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerDebtServiceImpl implements CustomerDebtService {

    private final CustomerRepository customerRepository;
    private final CustomerDebtRepository customerDebtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final CustomerDebtMapper customerDebtMapper;

    @Override
    public PageResponse<CustomerResponse> searchCustomers(String query, Pageable pageable) {
        Page<CustomerResponse> responses = customerRepository.search(query, pageable)
                .map(customerDebtMapper::toResponse);
        return PageResponse.from(responses);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerDebtMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse getCustomer(UUID id) {
        return customerDebtMapper.toResponse(findCustomer(id));
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = customerDebtMapper.toEntity(request);
        customer = customerRepository.save(customer);
        return customerDebtMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request) {
        Customer customer = findCustomer(id);
        customerDebtMapper.updateEntity(request, customer);
        customer = customerRepository.save(customer);
        return customerDebtMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        Customer customer = findCustomer(id);
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdOrderByCreatedAtDesc(id);
        if (!debts.isEmpty()) {
            throw new BusinessRuleViolationException("Cannot delete customer with debt records");
        }
        customerRepository.delete(customer);
    }

    @Override
    public BigDecimal getCustomerBalance(UUID id) {
        return findCustomer(id).getTotalDebt();
    }

    @Override
    public PageResponse<CustomerDebtResponse> getAllDebts(UUID customerId, DebtStatus status, String search, Pageable pageable) {
        Page<CustomerDebtResponse> responses = customerDebtRepository.search(customerId, status, search, pageable)
                .map(customerDebtMapper::toDebtResponse);
        return PageResponse.from(responses);
    }

    @Override
    public CustomerDebtResponse getDebt(UUID id) {
        return customerDebtMapper.toDebtResponse(findDebt(id));
    }

    @Override
    public List<CustomerDebtResponse> getCustomerDebtHistory(UUID customerId) {
        findCustomer(customerId);
        return customerDebtRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(customerDebtMapper::toDebtResponse)
                .toList();
    }

    @Override
    @Transactional
    public DebtPaymentResponse recordPayment(UUID debtId, RecordDebtPaymentRequest request) {
        CustomerDebt debt = findDebt(debtId);

        if (debt.getStatus() == DebtStatus.PAID) {
            throw new BusinessRuleViolationException("Debt is already fully paid");
        }

        BigDecimal paymentAmount = request.amount();
        if (paymentAmount.compareTo(debt.getAmount()) > 0) {
            throw new BusinessRuleViolationException(
                    "Payment amount (%.2f) exceeds remaining debt amount (%.2f)".formatted(paymentAmount, debt.getAmount()));
        }

        BigDecimal newRemaining = debt.getAmount().subtract(paymentAmount);
        BigDecimal newPaid = debt.getPaidAmount().add(paymentAmount);

        debt.setAmount(newRemaining);
        debt.setPaidAmount(newPaid);
        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(DebtStatus.PAID);
        } else {
            debt.setStatus(DebtStatus.PARTIALLY_PAID);
        }

        debt = customerDebtRepository.save(debt);

        // Update customer balance if customer is associated
        if (debt.getCustomer() != null) {
            Customer customer = debt.getCustomer();
            BigDecimal updatedBalance = customer.getTotalDebt().subtract(paymentAmount);
            if (updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
                updatedBalance = BigDecimal.ZERO;
            }
            customer.setTotalDebt(updatedBalance);
            customerRepository.save(customer);
        }

        DebtPayment payment = DebtPayment.builder()
                .debt(debt)
                .amount(paymentAmount)
                .paymentType(request.paymentType() != null ? request.paymentType() : PaymentType.CASH)
                .referenceNumber(request.referenceNumber())
                .notes(request.notes())
                .build();

        payment = debtPaymentRepository.save(payment);

        return customerDebtMapper.toPaymentResponse(payment);
    }

    @Override
    public List<DebtPaymentResponse> getPaymentHistory(UUID debtId) {
        findDebt(debtId);
        return debtPaymentRepository.findByDebtIdOrderByCreatedAtDesc(debtId).stream()
                .map(customerDebtMapper::toPaymentResponse)
                .toList();
    }

    private Customer findCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private CustomerDebt findDebt(UUID id) {
        return customerDebtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerDebt", id));
    }
}
