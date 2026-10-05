<?php
// backend/flamepro_api/api/products/get_products.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use GET."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$category = isset($_GET['category']) ? trim($_GET['category']) : '';
$search = isset($_GET['search']) ? trim($_GET['search']) : '';

try {
    $query = "SELECT id, name, price, original_price, discount, rating, reviews, 
                     image_name, weight, type, coverage, key_features, category_tag, in_stock 
              FROM products 
              WHERE 1=1";
    $params = [];

    if (!empty($category) && $category !== 'All') {
        $query .= " AND LOWER(category_tag) = LOWER(:category)";
        $params[':category'] = $category;
    }

    if (!empty($search)) {
        $query .= " AND (LOWER(name) LIKE :search OR LOWER(category_tag) LIKE :search)";
        $params[':search'] = '%' . strtolower($search) . '%';
    }

    $query .= " ORDER BY id ASC";

    $stmt = $db->prepare($query);
    $stmt->execute($params);
    $products = $stmt->fetchAll();

    // Decode json key_features into PHP arrays
    foreach ($products as &$prod) {
        if (isset($prod['key_features']) && is_string($prod['key_features'])) {
            $prod['key_features'] = json_decode($prod['key_features'], true);
        }
        $prod['rating'] = floatval($prod['rating']);
        $prod['reviews'] = intval($prod['reviews']);
        $prod['in_stock'] = (bool)$prod['in_stock'];
    }

    echo json_encode([
        "success" => true,
        "count" => count($products),
        "products" => $products
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
