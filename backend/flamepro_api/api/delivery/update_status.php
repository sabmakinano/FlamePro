<?php
// backend/flamepro_api/api/delivery/update_status.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use POST."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$data = json_decode(file_get_contents("php://input"), true) ?: $_POST;

$orderId = isset($data['order_id']) ? intval($data['order_id']) : 0;
$status = isset($data['status']) ? trim($data['status']) : '';

if ($orderId <= 0 || empty($status)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "order_id and status are required."]);
    exit();
}

try {
    $deliveryStatus = ($status === 'Delivered') ? 'Delivered' : 'In Transit';
    $stmt = $db->prepare("UPDATE orders SET status = :status, delivery_status = :delivery_status WHERE id = :order_id");
    $stmt->execute([
        ':status' => $status,
        ':delivery_status' => $deliveryStatus,
        ':order_id' => $orderId
    ]);

    echo json_encode([
        "success" => true,
        "message" => "Order delivery status updated to $status."
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
