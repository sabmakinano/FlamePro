<?php
// backend/flamepro_api/api/orders/create_order.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use POST."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$rawInput = file_get_contents("php://input");
$data = json_decode($rawInput, true) ?: $_POST;

$userId = isset($data['user_id']) ? intval($data['user_id']) : null;
$totalPrice = isset($data['total_price']) ? trim($data['total_price']) : '';
$paymentMethod = isset($data['payment_method']) ? trim($data['payment_method']) : 'Cash on Delivery';
$shippingAddress = isset($data['shipping_address']) ? trim($data['shipping_address']) : '';
$items = isset($data['items']) && is_array($data['items']) ? $data['items'] : [];

if (empty($totalPrice) || empty($items)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Total price and at least one order item are required."]);
    exit();
}

try {
    $db->beginTransaction();

    // Generate unique order number (e.g., FP-20260918-XXXX)
    $orderNumber = 'FP-' . date('Ymd') . '-' . strtoupper(substr(uniqid(), -4));

    $orderSql = "INSERT INTO orders (user_id, order_number, total_price, payment_method, shipping_address, status) 
                 VALUES (:user_id, :order_number, :total_price, :payment_method, :shipping_address, 'Pending') 
                 RETURNING id, order_number, total_price, payment_method, status, created_at";
    $orderStmt = $db->prepare($orderSql);
    $orderStmt->execute([
        ':user_id' => $userId,
        ':order_number' => $orderNumber,
        ':total_price' => $totalPrice,
        ':payment_method' => $paymentMethod,
        ':shipping_address' => $shippingAddress
    ]);
    $order = $orderStmt->fetch();
    $orderId = $order['id'];

    // Insert items
    $itemSql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price) 
                VALUES (:order_id, :product_id, :product_name, :quantity, :unit_price)";
    $itemStmt = $db->prepare($itemSql);

    foreach ($items as $item) {
        $itemStmt->execute([
            ':order_id' => $orderId,
            ':product_id' => isset($item['product_id']) ? $item['product_id'] : null,
            ':product_name' => isset($item['product_name']) ? $item['product_name'] : 'Unknown Product',
            ':quantity' => isset($item['quantity']) ? intval($item['quantity']) : 1,
            ':unit_price' => isset($item['unit_price']) ? $item['unit_price'] : '₱ 0.00'
        ]);
    }

    $db->commit();

    http_response_code(201);
    echo json_encode([
        "success" => true,
        "message" => "Order placed successfully.",
        "order" => $order
    ]);
} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Failed to create order: " . $e->getMessage()]);
}
