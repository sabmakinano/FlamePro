<?php
// backend/flamepro_api/api/delivery/get_assigned_orders.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use GET."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$riderId = isset($_GET['rider_id']) ? intval($_GET['rider_id']) : 0;

try {
    $sql = "SELECT o.id, o.order_number, o.total_price, o.payment_method, o.shipping_address, 
                   o.status, o.delivery_status, o.created_at,
                   u.first_name, u.last_name, u.phone_number AS customer_phone
            FROM orders o
            LEFT JOIN users u ON o.user_id = u.id ";

    $params = [];
    if ($riderId > 0) {
        $sql .= "WHERE o.assigned_delivery_id = :rider_id ";
        $params[':rider_id'] = $riderId;
    }

    $sql .= "ORDER BY o.created_at DESC";

    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $orders = $stmt->fetchAll();

    $itemStmt = $db->prepare("SELECT product_name, quantity, unit_price FROM order_items WHERE order_id = :order_id");

    foreach ($orders as &$ord) {
        $itemStmt->execute([':order_id' => $ord['id']]);
        $ord['items'] = $itemStmt->fetchAll();
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
