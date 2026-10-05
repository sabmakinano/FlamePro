-- ==========================================================
-- FlamePro Database Schema (PostgreSQL 14+)
-- Use pgAdmin 4:
-- 1. Create a database named: flamepro_db
-- 2. Right click flamepro_db -> Query Tool
-- 3. Paste this script and execute (press F5 or the Play button)
-- ==========================================================

-- Drop tables if they already exist (in reverse dependency order)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 1. Users Table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    username VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) DEFAULT '',
    last_name VARCHAR(100) DEFAULT '',
    middle_name VARCHAR(100) DEFAULT '',
    phone_number VARCHAR(50) DEFAULT '',
    address VARCHAR(255) DEFAULT '',
    barangay VARCHAR(100) DEFAULT '',
    city VARCHAR(100) DEFAULT '',
    province VARCHAR(100) DEFAULT '',
    profile_image_url TEXT DEFAULT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Categories Table
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Products Table
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price VARCHAR(50) NOT NULL,
    original_price VARCHAR(50) NOT NULL,
    discount VARCHAR(50) DEFAULT '0% OFF',
    rating NUMERIC(3, 1) DEFAULT 5.0,
    reviews INT DEFAULT 0,
    image_name VARCHAR(100) DEFAULT 'logo',
    weight VARCHAR(50),
    type VARCHAR(100),
    coverage VARCHAR(100),
    key_features JSONB DEFAULT '["UL Listed & Certified", "Standard Safety Compliance", "1-Year Warranty"]'::jsonb,
    category_tag VARCHAR(100),
    in_stock BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Orders Table
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    total_price VARCHAR(50) NOT NULL,
    payment_method VARCHAR(100) DEFAULT 'Cash on Delivery',
    shipping_address TEXT,
    status VARCHAR(50) DEFAULT 'Pending',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Order Items Table
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id) ON DELETE CASCADE,
    product_id INT REFERENCES products(id) ON DELETE SET NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================
-- SEED DATA
-- ==========================================================

-- Insert Categories
INSERT INTO categories (name) VALUES
    ('Fire Extinguishers'),
    ('Fire Sprinklers'),
    ('Fireman Equipments'),
    ('Fire Hose'),
    ('Others');

-- Insert Products
INSERT INTO products (name, price, original_price, discount, rating, reviews, image_name, weight, type, coverage, category_tag, in_stock) VALUES
    ('ABC Dry Powder 5 lb', '₱ 45.00', '₱ 55.00', '18% OFF', 4.8, 24, 'logo', '5 lb', 'Dry Powder', '10-15 ft', 'Fire Extinguishers', TRUE),
    ('CO2 Extinguisher 10 lb', '₱ 89.99', '₱ 110.00', '18% OFF', 4.7, 15, 'logo', '10 lb', 'CO2', '8-12 ft', 'Fire Extinguishers', TRUE),
    ('Foam Extinguisher 9 L', '₱ 59.50', '₱ 75.00', '20% OFF', 4.5, 10, 'logo', '9L', 'Foam', '12-18 ft', 'Fire Extinguishers', TRUE),
    ('Automatic Sprinkler Head', '₱ 24.99', '₱ 30.00', '16% OFF', 4.9, 42, 'logo', '0.5 lb', 'Automatic', '200 sq ft', 'Fire Sprinklers', TRUE),
    ('Fireman Helmet (Pro)', '₱ 120.00', '₱ 150.00', '20% OFF', 5.0, 8, 'logo', '3 lb', 'Protective', 'N/A', 'Fireman Equipments', TRUE),
    ('Fire Hose Reel 30 m', '₱ 199.99', '₱ 250.00', '20% OFF', 4.6, 5, 'logo', '15 lb', 'Manual', '30m', 'Fire Hose', TRUE),
    ('Fire Blanket 1.2 x 1.2 m', '₱ 15.99', '₱ 20.00', '20% OFF', 4.8, 56, 'logo', '1 lb', 'Fiberglass', '1.2x1.2m', 'Fire Extinguishers', TRUE),
    ('Smoke Detector (Battery)', '₱ 12.50', '₱ 18.00', '30% OFF', 4.4, 120, 'logo', '0.3 lb', 'Ionization', 'Room', 'Others', TRUE),
    ('Fire Exit Sign (LED)', '₱ 35.00', '₱ 45.00', '22% OFF', 4.7, 30, 'logo', '2 lb', 'LED Emergency', 'Visual', 'Others', TRUE),
    ('Fireman Suit (Standard)', '₱ 450.00', '₱ 550.00', '18% OFF', 4.9, 3, 'logo', '12 lb', 'Heat Resistant', 'Body', 'Fireman Equipments', TRUE),
    ('Fire Hose Nozzle (Brass)', '₱ 42.00', '₱ 55.00', '23% OFF', 4.5, 18, 'logo', '2 lb', 'Adjustable', 'Variable', 'Fire Hose', TRUE),
    ('Water Extinguisher 6 L', '₱ 39.99', '₱ 50.00', '20% OFF', 4.3, 12, 'logo', '6L', 'Water', '15-20 ft', 'Fire Extinguishers', TRUE),
    ('Wet Chemical Extinguisher', '₱ 75.00', '₱ 95.00', '21% OFF', 4.8, 9, 'logo', '6L', 'Wet Chemical', '10-12 ft', 'Fire Extinguishers', TRUE),
    ('Sprinkler Pipe 2 m', '₱ 18.50', '₱ 25.00', '26% OFF', 4.2, 20, 'logo', '5 lb', 'Steel', '2m', 'Fire Sprinklers', TRUE),
    ('Fireman Boots', '₱ 85.00', '₱ 110.00', '22% OFF', 4.7, 14, 'logo', '4 lb', 'Waterproof', 'Feet', 'Fireman Equipments', TRUE),
    ('Fire Axe (Heavy Duty)', '₱ 55.00', '₱ 70.00', '21% OFF', 4.6, 22, 'logo', '6 lb', 'Steel', 'N/A', 'Fireman Equipments', TRUE),
    ('Fire Hose Cabinet', '₱ 145.00', '₱ 180.00', '19% OFF', 4.5, 7, 'logo', '20 lb', 'Metal', 'Standard', 'Fire Hose', TRUE),
    ('First Aid Kit (Large)', '₱ 65.00', '₱ 85.00', '23% OFF', 4.9, 45, 'logo', '5 lb', 'Emergency', 'Medical', 'Others', TRUE),
    ('Fire Whistle', '₱ 5.99', '₱ 10.00', '40% OFF', 4.0, 80, 'logo', '0.1 lb', 'Alert', 'Audible', 'Fireman Equipments', TRUE),
    ('Gas Mask (Single Filter)', '₱ 95.00', '₱ 120.00', '20% OFF', 4.8, 11, 'logo', '2 lb', 'Air Purifying', 'Head', 'Fireman Equipments', TRUE);

-- Create a default test user (password is '123456')
-- Hash created with PHP password_hash('123456', PASSWORD_BCRYPT)
INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, city, province)
VALUES (
    'glenmarkcasayas@gmail.com',
    'glenmark',
    '$2y$10$eA89qKx0m.R82O9pW7zXEuF5ZkJv0jBqO6Lh0J5Y4S8w0nZzD8tW6',
    'Glen',
    'Casayas',
    '09123456789',
    'Cebu City',
    'Cebu'
);
