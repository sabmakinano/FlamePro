<?php
// backend/flamepro_api/api/auth/login.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use POST."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

// Parse JSON input or POST form
$rawInput = file_get_contents("php://input");
$data = json_decode($rawInput, true);

if (!$data) {
    $data = $_POST;
}

$usernameOrEmail = isset($data['username_or_email']) ? trim($data['username_or_email']) : '';
if (empty($usernameOrEmail) && isset($data['email'])) {
    $usernameOrEmail = trim($data['email']);
}
if (empty($usernameOrEmail) && isset($data['username'])) {
    $usernameOrEmail = trim($data['username']);
}
$password = isset($data['password']) ? trim($data['password']) : '';

if (empty($usernameOrEmail) || empty($password)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Username/Email and Password are required."]);
    exit();
}

try {
    $sql = "SELECT id, email, username, password_hash, first_name, last_name, middle_name, 
                   phone_number, address, barangay, city, province, profile_image_url,
                   role, is_active
            FROM users 
            WHERE LOWER(username) = LOWER(:input) OR LOWER(email) = LOWER(:input) 
            LIMIT 1";
            
    $stmt = $db->prepare($sql);
    $stmt->execute([':input' => $usernameOrEmail]);
    $user = $stmt->fetch();

    if (!$user || !password_verify($password, $user['password_hash'])) {
        http_response_code(401);
        echo json_encode(["success" => false, "message" => "Invalid email/username or password."]);
        exit();
    }

    // Unset sensitive data
    unset($user['password_hash']);

    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Login successful.",
        "user" => $user
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
