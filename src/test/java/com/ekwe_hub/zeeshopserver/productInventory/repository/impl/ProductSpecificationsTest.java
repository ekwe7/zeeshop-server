package com.ekwe_hub.zeeshopserver.productInventory.repository.impl;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.CategoryRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.ProductRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.UnitRepository;
import com.ekwe_hub.zeeshopserver.shared.infrastructure.persistence.SystemAuditorAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises ProductSpecifications.matching against a real (H2) database via
 * ProductRepository.findAll(Specification, Pageable) — the standard
 * JpaSpecificationExecutor method, not a hand-written fragment. Nothing here
 * is mocked, since the point is to prove the actual LIKE/equals predicates
 * work against persisted rows, not just that they were called.
 */
@DataJpaTest
@Import(SystemAuditorAware.class)
@ActiveProfiles("test")
class ProductSpecificationsTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    private Category beverages;
    private Category snacks;
    private Unit kilogram;
    private Unit liter;

    @BeforeEach
    void setUp() {
        beverages = categoryRepository.save(Category.builder().name("Beverages").description("Drinks").build());
        snacks = categoryRepository.save(Category.builder().name("Snacks").description("Chips and such").build());
        kilogram = unitRepository.save(Unit.builder().name("Kilogram").symbol("kg").build());
        liter = unitRepository.save(Unit.builder().name("Liter").symbol("l").build());

        productRepository.save(product("Coke Classic", "SKU-001", beverages, liter));
        productRepository.save(product("Diet Coke", "SKU-002", beverages, kilogram));
        productRepository.save(product("Potato Chips", "SKU-003", snacks, kilogram));
    }

    private Product product(String name, String sku, Category category, Unit unit) {
        return Product.builder()
                .name(name)
                .sku(sku)
                .description("desc")
                .price(BigDecimal.TEN)
                .category(category)
                .unit(unit)
                .build();
    }

    @Test
    void matching_returnsAllProducts_whenNoFiltersApplied() {
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.matching(null, null, null), PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Coke Classic", "Diet Coke", "Potato Chips");
    }

    @Test
    void matching_filtersByName_caseInsensitivePartialMatch() {
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.matching("coke", null, null), PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Coke Classic", "Diet Coke");
    }

    @Test
    void matching_blankName_isTreatedAsNoFilter() {
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.matching("   ", null, null), PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void matching_filtersByCategoryId() {
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.matching(null, snacks.getId(), null), PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactly("Potato Chips");
    }

    @Test
    void matching_filtersByUnitId() {
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.matching(null, null, liter.getId()), PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactly("Coke Classic");
    }

    @Test
    void matching_combinesNameCategoryAndUnitFilters_withAndSemantics() {
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.matching("coke", beverages.getId(), kilogram.getId()), PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactly("Diet Coke");
    }

    @Test
    void matching_returnsEmptyPage_whenNoProductMatchesAllFilters() {
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.matching("coke", snacks.getId(), null), PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void matching_respectsPageableSizeAndSort() {
        Page<Product> firstPage = productRepository.findAll(
                ProductSpecifications.matching(null, null, null), PageRequest.of(0, 2, Sort.by("name")));

        assertThat(firstPage.getContent()).extracting(Product::getName)
                .containsExactly("Coke Classic", "Diet Coke");
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();
    }
}
