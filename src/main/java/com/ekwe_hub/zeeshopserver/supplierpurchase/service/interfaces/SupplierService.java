package com.ekwe_hub.zeeshopserver.supplierpurchase.service.interfaces;

import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.SupplierResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SupplierService {

    List<SupplierResponse> getAllSuppliers();

    SupplierResponse getSupplier(UUID id);

    SupplierResponse createSupplier(CreateSupplierRequest request);

    SupplierResponse updateSupplier(UUID id, UpdateSupplierRequest request);

    void deleteSupplier(UUID id);

    BigDecimal getBalance(UUID id);
}
