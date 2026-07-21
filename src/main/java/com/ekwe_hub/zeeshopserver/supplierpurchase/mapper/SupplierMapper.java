package com.ekwe_hub.zeeshopserver.supplierpurchase.mapper;

import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdateSupplierRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.SupplierResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toEntity(CreateSupplierRequest request) {
        return Supplier.builder()
                .name(request.name())
                .contactName(request.contactName())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .build();
    }

    public void updateEntity(UpdateSupplierRequest request, Supplier supplier) {
        supplier.setName(request.name());
        supplier.setContactName(request.contactName());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setAddress(request.address());
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .balance(supplier.getBalance())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
