<?php
// backend/flamepro_api/api/services/create_service_request.php

require_once __DIR__ . '/../../config/db.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use POST."]);
    exit();
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid JSON input."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$userId = isset($input['user_id']) ? intval($input['user_id']) : null;
$serviceType = trim($input['service_type'] ?? '');
$contact = trim($input['contact_number'] ?? '');
$address = trim($input['address'] ?? '');
$serviceDate = trim($input['service_date'] ?? '');
$serviceTime = trim($input['service_time'] ?? '');
$details = trim($input['details'] ?? '');

if (empty($serviceType) || empty($contact) || empty($address)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Required fields missing."]);
    exit();
}

try {
    // Ensure table exists
    $db->exec("CREATE TABLE IF NOT EXISTS service_requests (
        id SERIAL PRIMARY KEY,
        user_id INT REFERENCES users(id) ON DELETE CASCADE,
        service_type VARCHAR(100) NOT NULL,
        details TEXT,
        service_date VARCHAR(50),
        service_time VARCHAR(50),
        address TEXT,
        contact_number VARCHAR(50),
        status VARCHAR(50) DEFAULT 'Pending',
        assigned_technician_id INT REFERENCES users(id) ON DELETE SET NULL,
        created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );");

    $sql = "INSERT INTO service_requests (user_id, service_type, details, service_date, service_time, address, contact_number, status)
            VALUES (:user_id, :service_type, :details, :service_date, :service_time, :address, :contact_number, 'Pending')
            RETURNING id";

    $stmt = $db->prepare($sql);
    $stmt->execute([
        ':user_id' => $userId,
        ':service_type' => $serviceType,
        ':details' => $details,
        ':service_date' => $serviceDate,
        ':service_time' => $serviceTime,
        ':address' => $address,
        ':contact_number' => $contact
    ]);

    $result = $stmt->fetch();
    $newId = $result ? $result['id'] : 0;

    echo json_encode([
        "success" => true,
        "message" => "Service request created successfully.",
        "request_id" => $newId
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database error: " . $e->getMessage()]);
}
