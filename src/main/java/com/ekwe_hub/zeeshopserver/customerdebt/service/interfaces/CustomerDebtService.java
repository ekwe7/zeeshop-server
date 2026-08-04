package com.ekwe_hub.zeeshopserver.customerdebt.service.interfaces;

import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.CreateCustomerRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.RecordDebtPaymentRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.request.UpdateCustomerRequest;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerDebtResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.CustomerResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.dto.response.DebtPaymentResponse;
import com.ekwe_hub.zeeshopserver.customerdebt.entity.DebtStatus;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CustomerDebtService {

    // Customer CRUD & Search
    PageResponse<CustomerResponse> searchCustomers(String query, Pageable pageable);
    List<CustomerResponse> getAllCustomers();
    CustomerResponse getCustomer(UUID id);
    CustomerResponse createCustomer(CreateCustomerRequest request);
    CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request);
    void deleteCustomer(UUID id);
    BigDecimal getCustomerBalance(UUID id);

    // Debt Records, Payment & History
    PageResponse<CustomerDebtResponse> getAllDebts(UUID customerId, DebtStatus status, String search, Pageable pageable);
    CustomerDebtResponse getDebt(UUID id);
    List<CustomerDebtResponse> getCustomerDebtHistory(UUID customerId);
    DebtPaymentResponse recordPayment(UUID debtId, RecordDebtPaymentRequest request);
    List<DebtPaymentResponse> getPaymentHistory(UUID debtId);
}
