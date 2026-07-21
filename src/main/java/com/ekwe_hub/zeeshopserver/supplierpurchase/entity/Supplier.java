package com.ekwe_hub.zeeshopserver.supplierpurchase.entity;

import com.ekwe_hub.zeeshopserver.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A vendor ZeeShop buys stock from. Balance is the running amount owed to
 * this supplier for completed purchases — it increases when a Purchase is
 * fully received (see PurchaseService) and is otherwise read-only from the
 * API; there is no payment-recording flow yet.
 */
@Getter
@Setter
@Entity
@Table(name = "suppliers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
}
