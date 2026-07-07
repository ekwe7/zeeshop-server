package com.ekwe_hub.zeeshopserver.productinventory.repository;

import com.ekwe_hub.zeeshopserver.productinventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
