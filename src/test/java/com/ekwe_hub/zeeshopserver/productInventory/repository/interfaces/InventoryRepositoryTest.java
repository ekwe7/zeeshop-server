package com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.shared.infrastructure.persistence.SystemAuditorAware;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * findLowStock() compares two columns on the same row (quantityOnHand vs.
 * lowStockThreshold), so a mocked repository test can't prove the generated
 * SQL is right — only a real query against a real database can.
 */
@DataJpaTest
@Import(SystemAuditorAware.class)
@ActiveProfiles("test")
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Test
    void findLowStock_returnsOnlyInventoryAtOrBelowItsOwnThreshold() {
        Category category = categoryRepository.save(Category.builder().name("Beverages").build());
        Unit unit = unitRepository.save(Unit.builder().name("Kilogram").symbol("kg").build());

        Product lowStockProduct = productRepository.save(product("Coke", "SKU-001", category, unit));
        Product healthyStockProduct = productRepository.save(product("Sprite", "SKU-002", category, unit));
        Product exactlyAtThresholdProduct = productRepository.save(product("Fanta", "SKU-003", category, unit));

        inventoryRepository.save(Inventory.builder().product(lowStockProduct).quantityOnHand(2).lowStockThreshold(5).build());
        inventoryRepository.save(Inventory.builder().product(healthyStockProduct).quantityOnHand(50).lowStockThreshold(5).build());
        inventoryRepository.save(Inventory.builder().product(exactlyAtThresholdProduct).quantityOnHand(5).lowStockThreshold(5).build());

        var result = inventoryRepository.findLowStock();

        assertThat(result).extracting(i -> i.getProduct().getName())
                .containsExactlyInAnyOrder("Coke", "Fanta");
    }

    @Test
    void findLowStock_returnsEmpty_whenNothingIsAtOrBelowThreshold() {
        Category category = categoryRepository.save(Category.builder().name("Beverages").build());
        Unit unit = unitRepository.save(Unit.builder().name("Kilogram").symbol("kg").build());
        Product healthyProduct = productRepository.save(product("Coke", "SKU-001", category, unit));

        inventoryRepository.save(Inventory.builder().product(healthyProduct).quantityOnHand(50).lowStockThreshold(5).build());

        assertThat(inventoryRepository.findLowStock()).isEmpty();
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
}
