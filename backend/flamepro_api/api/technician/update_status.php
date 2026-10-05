<?php
// backend/flamepro_api/api/technician/update_status.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use POST."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$data = json_decode(file_get_contents("php://input"), true) ?: $_POST;

$serviceId = isset($data['service_id']) ? intval($data['service_id']) : 0;
$status = isset($data['status']) ? trim($data['status']) : '';

if ($serviceId <= 0 || empty($status)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "service_id and status are required."]);
    exit();
}

try {
    $stmt = $db->prepare("UPDATE service_requests SET status = :status WHERE id = :service_id");
    $stmt->execute([
        ':status' => $status,
        ':service_id' => $serviceId
    ]);

    echo json_encode([
        "success" => true,
        "message" => "Service request status updated to $status."
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
