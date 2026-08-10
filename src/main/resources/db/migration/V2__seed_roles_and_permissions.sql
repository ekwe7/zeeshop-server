-- Flyway V2: Dynamic role & permission seeding by role name
-- Fixes foreign key constraint issue when roles were created with dynamic UUIDs.

-- 1. Ensure roles exist in case they are missing
INSERT INTO roles (id, name, created_at, updated_at, created_by, updated_by)
SELECT gen_random_uuid(), 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (id, name, created_at, updated_at, created_by, updated_by)
SELECT gen_random_uuid(), 'MANAGER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'MANAGER');

INSERT INTO roles (id, name, created_at, updated_at, created_by, updated_by)
SELECT gen_random_uuid(), 'CASHIER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CASHIER');

-- 2. Seed ADMIN permissions dynamically using roles.id lookup
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm
FROM roles r
CROSS JOIN (
    SELECT 'USER_READ' AS perm UNION ALL
    SELECT 'USER_WRITE' UNION ALL
    SELECT 'ROLE_MANAGE' UNION ALL
    SELECT 'SALES_READ' UNION ALL
    SELECT 'SALES_WRITE' UNION ALL
    SELECT 'INVENTORY_READ' UNION ALL
    SELECT 'INVENTORY_WRITE' UNION ALL
    SELECT 'SUPPLIER_READ' UNION ALL
    SELECT 'SUPPLIER_WRITE' UNION ALL
    SELECT 'CUSTOMER_DEBT_READ' UNION ALL
    SELECT 'CUSTOMER_DEBT_WRITE' UNION ALL
    SELECT 'EXPENSE_READ' UNION ALL
    SELECT 'EXPENSE_WRITE'
) p
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm
  );

-- 3. Seed MANAGER permissions dynamically using roles.id lookup
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm
FROM roles r
CROSS JOIN (
    SELECT 'USER_READ' AS perm UNION ALL
    SELECT 'SALES_READ' UNION ALL
    SELECT 'SALES_WRITE' UNION ALL
    SELECT 'INVENTORY_READ' UNION ALL
    SELECT 'INVENTORY_WRITE' UNION ALL
    SELECT 'SUPPLIER_READ' UNION ALL
    SELECT 'SUPPLIER_WRITE' UNION ALL
    SELECT 'CUSTOMER_DEBT_READ' UNION ALL
    SELECT 'CUSTOMER_DEBT_WRITE' UNION ALL
    SELECT 'EXPENSE_READ' UNION ALL
    SELECT 'EXPENSE_WRITE'
) p
WHERE r.name = 'MANAGER'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm
  );

-- 4. Seed CASHIER permissions dynamically using roles.id lookup
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm
FROM roles r
CROSS JOIN (
    SELECT 'SALES_READ' AS perm UNION ALL
    SELECT 'SALES_WRITE' UNION ALL
    SELECT 'INVENTORY_READ' UNION ALL
    SELECT 'CUSTOMER_DEBT_READ' UNION ALL
    SELECT 'CUSTOMER_DEBT_WRITE'
) p
WHERE r.name = 'CASHIER'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm
  );
