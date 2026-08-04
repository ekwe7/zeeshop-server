package com.ekwe_hub.zeeshopserver.sales.controller;

import com.ekwe_hub.zeeshopserver.sales.dto.request.CreateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.request.UpdateSaleRequest;
import com.ekwe_hub.zeeshopserver.sales.dto.response.SaleResponse;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleStatus;
import com.ekwe_hub.zeeshopserver.sales.service.interfaces.ReceiptService;
import com.ekwe_hub.zeeshopserver.sales.service.interfaces.SaleService;
import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final ReceiptService receiptService;

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getAllSales(
            @RequestParam(required = false) SaleStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getAllSales(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<ApiResponse<SaleResponse>> getSale(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSale(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(@Valid @RequestBody CreateSaleRequest request) {
        SaleResponse created = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<SaleResponse>> updateSale(@PathVariable UUID id,
                                                                 @Valid @RequestBody UpdateSaleRequest request) {
        SaleResponse updated = saleService.updateSale(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Sale updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<Void>> deleteSale(@PathVariable UUID id) {
        saleService.deleteSale(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sale deleted successfully"));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<SaleResponse>> completeSale(@PathVariable UUID id) {
        SaleResponse updated = saleService.completeSale(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Sale completed successfully"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SALES_WRITE')")
    public ResponseEntity<ApiResponse<SaleResponse>> cancelSale(@PathVariable UUID id) {
        SaleResponse updated = saleService.cancelSale(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Sale cancelled successfully"));
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAuthority('SALES_READ')")
    public ResponseEntity<byte[]> getSaleReceipt(@PathVariable UUID id) {
        byte[] pdfContent = receiptService.generateReceiptPdf(id);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receipt-" + id + ".pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
}
