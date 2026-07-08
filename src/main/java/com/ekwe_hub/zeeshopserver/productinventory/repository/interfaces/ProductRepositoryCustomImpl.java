package com.ekwe_hub.zeeshopserver.productinventory.repository.interfaces;

import com.ekwe_hub.zeeshopserver.productinventory.entity.Product;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.UUID;

/**
 * Extends SimpleJpaRepository purely to reuse its Specification-aware
 * findAll(Specification, Pageable) — the same query support JpaSpecificationExecutor
 * would give ProductRepository directly, but exposed here as the one method
 * ProductRepository actually needs, with the WHERE-clause building kept private.
 *
 * Spring Data matches this class to the ProductRepositoryCustom fragment by
 * name (fragment interface name + "Impl") within the SAME package as the
 * fragment interface — that resolution is package-based, not classpath-wide,
 * so this class must stay next to ProductRepositoryCustom rather than moving
 * to repository/impl alongside the other implementation classes. It is not a
 * @Component and must not be annotated as one; the EntityManager is supplied
 * by Spring Data's repository factory.
 */
public class ProductRepositoryCustomImpl extends SimpleJpaRepository<Product, UUID> implements ProductRepositoryCustom {

    public ProductRepositoryCustomImpl(EntityManager entityManager) {
        super(Product.class, entityManager);
    }

    @Override
    public Page<Product> search(String name, UUID categoryId, UUID unitId, Pageable pageable) {
        return findAll(buildFilter(name, categoryId, unitId), pageable);
    }

    private Specification<Product> buildFilter(String name, UUID categoryId, UUID unitId) {
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
