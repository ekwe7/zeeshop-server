package com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces;

import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);
}
