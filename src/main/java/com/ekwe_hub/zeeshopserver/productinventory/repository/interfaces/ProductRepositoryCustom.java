package com.ekwe_hub.zeeshopserver.productinventory.repository.interfaces;

import com.ekwe_hub.zeeshopserver.productinventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Hand-written fragment for the product catalogue's dynamic filter/search.
 * Implemented by ProductRepositoryCustomImpl, kept in this same package —
 * Spring Data resolves a fragment's implementation by package + name
 * (<interface>Impl), so the two cannot be split across repository/interfaces
 * and repository/impl the way the other repositories are. Everything else on
 * ProductRepository is either inherited from JpaRepository or a Spring Data
 * derived-query method, which the framework implements itself.
 */
public interface ProductRepositoryCustom {

    Page<Product> search(String name, UUID categoryId, UUID unitId, Pageable pageable);
}
