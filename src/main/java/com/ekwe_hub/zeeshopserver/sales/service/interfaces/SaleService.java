package com.ekwe_hub.zeeshopserver.sales.service.interfaces;

import com.ekwe_hub.zeeshopserver.sales.dto.request.CreateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.UpdateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.response.SaleResponse;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleStatus;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SaleService {

    PageResponse<SaleResponse> getAllSales(SaleStatus status, Pageable pageable);

    SaleResponse getSale(UUID id);

    SaleResponse createSale(CreateSaleRequest request);

    SaleResponse updateSale(UUID id, UpdateSaleRequest request);

    void deleteSale(UUID id);

    SaleResponse completeSale(UUID id);

    SaleResponse cancelSale(UUID id);
}
