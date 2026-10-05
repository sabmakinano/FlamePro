<?php
// backend/flamepro_api/api/orders/get_orders.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use GET."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;

try {
    $sql = "SELECT id, user_id, order_number, total_price, payment_method, shipping_address, status, created_at 
            FROM orders ";
    $params = [];

    if ($userId !== null && $userId > 0) {
        $sql .= "WHERE user_id = :user_id ";
        $params[':user_id'] = $userId;
    }

    $sql .= "ORDER BY created_at DESC";

    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $orders = $stmt->fetchAll();

    // Fetch items for each order
    $itemStmt = $db->prepare("SELECT id, product_id, product_name, quantity, unit_price FROM order_items WHERE order_id = :order_id");

    foreach ($orders as &$order) {
        $itemStmt->execute([':order_id' => $order['id']]);
        $order['items'] = $itemStmt->fetchAll();
    }

    echo json_encode([
        "success" => true,
        "count" => count($orders),
        "orders" => $orders
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
