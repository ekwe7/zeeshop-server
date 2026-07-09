package com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Category;
import com.ekwe_hub.zeeshopserver.productInventory.entity.InventoryAdjustment;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Unit;
import com.ekwe_hub.zeeshopserver.shared.infrastructure.persistence.SystemAuditorAware;
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

@DataJpaTest
@Import(SystemAuditorAware.class)
@ActiveProfiles("test")
class InventoryAdjustmentRepositoryTest {

    @Autowired
    private InventoryAdjustmentRepository inventoryAdjustmentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Test
    void findByProductId_returnsOnlyThatProductsHistory_inCallerRequestedOrder() {
        // Sorted by "reason" (not createdAt) so the assertion doesn't depend on
        // wall-clock precision between two saves in the same test.
        Category category = categoryRepository.save(Category.builder().name("Beverages").build());
        Unit unit = unitRepository.save(Unit.builder().name("Kilogram").symbol("kg").build());
        Product coke = productRepository.save(product("Coke", "SKU-001", category, unit));
        Product sprite = productRepository.save(product("Sprite", "SKU-002", category, unit));

        inventoryAdjustmentRepository.save(adjustment(coke, 0, 10, "Initial stock"));
        inventoryAdjustmentRepository.save(adjustment(coke, 10, 15, "Restock"));
        inventoryAdjustmentRepository.save(adjustment(sprite, 0, 20, "Initial stock"));

        Page<InventoryAdjustment> result = inventoryAdjustmentRepository.findByProductId(
                coke.getId(), PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "reason")));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(InventoryAdjustment::getReason)
                .containsExactly("Initial stock", "Restock");
    }

    @Test
    void findByProductId_returnsEmptyPage_whenProductHasNoHistory() {
        Category category = categoryRepository.save(Category.builder().name("Beverages").build());
        Unit unit = unitRepository.save(Unit.builder().name("Kilogram").symbol("kg").build());
        Product coke = productRepository.save(product("Coke", "SKU-001", category, unit));

        Page<InventoryAdjustment> result = inventoryAdjustmentRepository.findByProductId(
                coke.getId(), PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
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

    private InventoryAdjustment adjustment(Product product, int before, int after, String reason) {
        return InventoryAdjustment.builder()
                .product(product)
                .quantityBefore(before)
                .quantityAfter(after)
                .reason(reason)
                .build();
    }
}
