INSERT INTO product_inventory (product_id, available_stock, version)
VALUES ('prod_flash_99', 100, 0)
ON CONFLICT (product_id) DO NOTHING;
