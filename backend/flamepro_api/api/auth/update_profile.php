<?php
// backend/flamepro_api/api/auth/update_profile.php

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

$id = isset($data['id']) ? intval($data['id']) : 0;
$email = isset($data['email']) ? trim($data['email']) : '';

if ($id <= 0 && empty($email)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "User identifier (id or email) is required."]);
    exit();
}

$firstName = isset($data['first_name']) ? trim($data['first_name']) : '';
$lastName = isset($data['last_name']) ? trim($data['last_name']) : '';
$middleName = isset($data['middle_name']) ? trim($data['middle_name']) : '';
$phoneNumber = isset($data['phone_number']) ? trim($data['phone_number']) : '';
$address = isset($data['address']) ? trim($data['address']) : '';
$barangay = isset($data['barangay']) ? trim($data['barangay']) : '';
$city = isset($data['city']) ? trim($data['city']) : '';
$province = isset($data['province']) ? trim($data['province']) : '';

try {
    $sql = "UPDATE users SET 
                first_name = COALESCE(NULLIF(:first_name, ''), first_name),
                last_name = COALESCE(NULLIF(:last_name, ''), last_name),
                middle_name = :middle_name,
                phone_number = COALESCE(NULLIF(:phone_number, ''), phone_number),
                address = :address,
                barangay = :barangay,
                city = :city,
                province = :province,
                updated_at = CURRENT_TIMESTAMP
            WHERE " . ($id > 0 ? "id = :id" : "LOWER(email) = LOWER(:email)") . "
            RETURNING id, email, username, first_name, last_name, middle_name, phone_number, address, barangay, city, province, profile_image_url";

    $stmt = $db->prepare($sql);
    $params = [
        ':first_name' => $firstName,
        ':last_name' => $lastName,
        ':middle_name' => $middleName,
        ':phone_number' => $phoneNumber,
        ':address' => $address,
        ':barangay' => $barangay,
        ':city' => $city,
        ':province' => $province
    ];
    if ($id > 0) {
        $params[':id'] = $id;
    } else {
        $params[':email'] = $email;
    }

    $stmt->execute($params);
    $updatedUser = $stmt->fetch();

    if (!$updatedUser) {
        http_response_code(404);
        echo json_encode(["success" => false, "message" => "User not found."]);
        exit();
    }

    echo json_encode([
        "success" => true,
        "message" => "Profile updated successfully.",
        "user" => $updatedUser
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
