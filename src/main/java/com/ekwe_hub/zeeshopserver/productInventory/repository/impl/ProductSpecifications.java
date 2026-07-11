package com.ekwe_hub.zeeshopserver.productInventory.repository.impl;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Builds the dynamic filter for the product catalogue search. A plain helper
 * class, not a Spring-managed bean — ProductRepository gets Specification
 * support for free by extending JpaSpecificationExecutor, so there is no
 * repository fragment/Impl wiring involved here, just a Specification value
 * handed to the inherited findAll(Specification, Pageable).
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> matching(String name, UUID categoryId, UUID unitId) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), categoryId));
            }
            if (unitId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("unit").get("id"), unitId));
            }

            return predicate;
        };
    }
}
