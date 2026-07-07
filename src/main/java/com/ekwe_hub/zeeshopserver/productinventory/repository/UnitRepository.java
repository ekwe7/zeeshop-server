package com.ekwe_hub.zeeshopserver.productinventory.repository;

import com.ekwe_hub.zeeshopserver.productinventory.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsBySymbol(String symbol);

    boolean existsBySymbolAndIdNot(String symbol, UUID id);
}
