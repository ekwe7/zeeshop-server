package com.ekwe_hub.zeeshopserver.customerdebt.repository;

import com.ekwe_hub.zeeshopserver.customerdebt.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    @Query("""
        SELECT c FROM Customer c
        WHERE (:query IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    Page<Customer> search(@Param("query") String query, Pageable pageable);
}
