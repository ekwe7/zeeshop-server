package com.ekwe_hub.zeeshopserver.productInventory.entity;

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

/**
 * Immutable audit trail row written every time adjustStock() runs — one row
 * per change, never updated afterwards. createdAt/createdBy from
 * AuditableEntity double as "when/who adjusted it"; updatedAt/updatedBy are
 * simply never touched.
 */
@Getter
@Setter
@Entity
@Table(name = "inventory_adjustments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAdjustment extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_before", nullable = false)
    private int quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private int quantityAfter;

    @Column(length = 255)
    private String reason;
}
