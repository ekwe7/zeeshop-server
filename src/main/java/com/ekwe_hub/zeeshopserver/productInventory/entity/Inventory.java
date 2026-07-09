package com.ekwe_hub.zeeshopserver.productInventory.entity;

import com.ekwe_hub.zeeshopserver.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tracks the stock level for exactly one Product. Kept as its own aggregate
 * (rather than columns on Product) so quantity can later evolve independently
 * — e.g. reservations, warehouse-level splits — without reshaping the catalogue.
 */
@Getter
@Setter
@Entity
@Table(name = "inventories")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private int quantityOnHand = 0;
}
