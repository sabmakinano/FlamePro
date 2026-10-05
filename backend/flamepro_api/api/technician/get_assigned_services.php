<?php
// backend/flamepro_api/api/technician/get_assigned_services.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use GET."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

$techId = isset($_GET['tech_id']) ? intval($_GET['tech_id']) : 0;

try {
    $sql = "SELECT s.id, s.service_type, s.details, s.service_date, s.service_time, 
                   s.address, s.contact_number, s.status, s.created_at,
                   u.first_name AS cust_first, u.last_name AS cust_last, u.phone_number AS cust_phone
            FROM service_requests s
            LEFT JOIN users u ON s.user_id = u.id ";

    $params = [];
    if ($techId > 0) {
        $sql .= "WHERE s.assigned_technician_id = :tech_id ";
        $params[':tech_id'] = $techId;
    }

    $sql .= "ORDER BY s.created_at DESC";

    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $services = $stmt->fetchAll();

    echo json_encode([
        "success" => true,
        "count" => count($services),
        "services" => $services
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
