package com.ekwe_hub.zeeshopserver.productInventory.mapper;

import com.ekwe_hub.zeeshopserver.productInventory.dto.request.CreateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.UpdateUnitRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.UnitResponse;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import org.springframework.stereotype.Component;

@Component
public class UnitMapper {

    public Unit toEntity(CreateUnitRequest request) {
        return Unit.builder()
                .name(request.name())
                .symbol(request.symbol())
                .build();
    }

    public void updateEntity(UpdateUnitRequest request, Unit unit) {
        unit.setName(request.name());
        unit.setSymbol(request.symbol());
    }

    public UnitResponse toResponse(Unit unit) {
        return UnitResponse.builder()
                .id(unit.getId())
                .name(unit.getName())
                .symbol(unit.getSymbol())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .build();
    }
}
