package com.ekwe_hub.zeeshopserver.productinventory.repository;

import com.ekwe_hub.zeeshopserver.productinventory.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Builds the dynamic WHERE clause for the product search/filter endpoint.
 * Each filter is optional (null = not applied), combined with AND — e.g.
 * name + categoryId together narrows to "products named X in category Y".
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> withFilters(String name, UUID categoryId, UUID unitId) {
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
