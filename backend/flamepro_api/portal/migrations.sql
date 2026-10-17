BEGIN;
-- Add remittance_status to orders if not exists
ALTER TABLE orders ADD COLUMN IF NOT EXISTS remittance_status VARCHAR(20) DEFAULT 'Pending';
-- Add stock_quantity and usage_guidelines to products if not exists
ALTER TABLE products ADD COLUMN IF NOT EXISTS stock_quantity INT DEFAULT 0;
ALTER TABLE products ADD COLUMN IF NOT EXISTS usage_guidelines TEXT;
COMMIT;
