package com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces;

import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Purchase;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseStatus;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.UUID;

/**
 * Extends SimpleJpaRepository purely to reuse its Specification-aware
 * findAll(Specification, Pageable), the same way ProductRepositoryCustomImpl
 * does for the product catalogue. Must stay in this package (not
 * repository/impl) for Spring Data's fragment-resolution-by-package rule,
 * and must not be a @Component — the repository factory supplies it.
 */
public class PurchaseRepositoryCustomImpl extends SimpleJpaRepository<Purchase, UUID> implements PurchaseRepositoryCustom {

    public PurchaseRepositoryCustomImpl(EntityManager entityManager) {
        super(Purchase.class, entityManager);
    }

    @Override
    public Page<Purchase> search(UUID supplierId, PurchaseStatus status, Pageable pageable) {
        return findAll(buildFilter(supplierId, status), pageable);
    }

    private Specification<Purchase> buildFilter(UUID supplierId, PurchaseStatus status) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (supplierId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("supplier").get("id"), supplierId));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            return predicate;
        };
    }
}
