<?php
// backend/flamepro_api/api/orders/cancel_order.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use POST."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
$orderId = isset($input['order_id']) ? intval($input['order_id']) : 0;

if ($orderId <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid order_id."]);
    exit();
}

try {
    $stmt = $db->prepare("UPDATE orders SET status = 'Cancelled' WHERE id = :order_id");
    $stmt->execute([':order_id' => $orderId]);
    echo json_encode(["success" => true, "message" => "Order cancelled."]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
