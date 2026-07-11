package com.ekwe_hub.zeeshopserver.productInventory.entity;

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
 * Groups products for browsing/reporting (e.g. "Beverages", "Stationery").
 * See CategoryController for CRUD; deletion is blocked while a Product still
 * references the category.
 */
@Getter
@Setter
@Entity
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;
}
