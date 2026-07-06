package com.ekwe_hub.zeeshopserver.userauth.entity;

/**
 * Fine-grained actions that can be granted to a Role.
 *
 * One READ/WRITE pair per business module (mirrors the top-level package
 * structure: sales, productinventory, supplierpurchase, customerdebt, expense)
 * plus two administrative permissions for managing users and roles themselves.
 *
 * Stored on Role as a Set<Permission> and exposed to Spring Security as
 * authorities (see UserPrincipal), so controllers can guard endpoints with
 * @PreAuthorize("hasAuthority('SALES_WRITE')") once those modules are built.
 */
public enum Permission {

    USER_READ,
    USER_WRITE,
    ROLE_MANAGE,

    SALES_READ,
    SALES_WRITE,

    INVENTORY_READ,
    INVENTORY_WRITE,

    SUPPLIER_READ,
    SUPPLIER_WRITE,

    CUSTOMER_DEBT_READ,
    CUSTOMER_DEBT_WRITE,

    EXPENSE_READ,
    EXPENSE_WRITE
}
