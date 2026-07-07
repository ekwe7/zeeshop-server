package com.ekwe_hub.zeeshopserver.productinventory.entity;

import com.ekwe_hub.zeeshopserver.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unit of measure a Product is stocked/sold in (e.g. "Kilogram" / "kg").
 * Like Category, this is a lookup entity referenced by id — no CRUD here.
 */
@Getter
@Setter
@Entity
@Table(name = "units")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;
}
