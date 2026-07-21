package com.ekwe_hub.zeeshopserver.supplierpurchase.entity;

import com.ekwe_hub.zeeshopserver.productinventory.entity.Product;
import com.ekwe_hub.zeeshopserver.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One product line on a Purchase. quantityReceived tracks partial receiving
 * independently of quantityOrdered — see PurchaseService.receiveStock, which
 * is the only place this field changes.
 */
@Getter
@Setter
@Entity
@Table(name = "purchase_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_ordered", nullable = false)
    private int quantityOrdered;

    @Column(name = "quantity_received", nullable = false)
    @Builder.Default
    private int quantityReceived = 0;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;
}
