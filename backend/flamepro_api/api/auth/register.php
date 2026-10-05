<?php
// backend/flamepro_api/api/auth/register.php

require_once __DIR__ . '/../../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed. Use POST."]);
    exit();
}

$database = new Database();
$db = $database->getConnection();

// Parse JSON input or form-data
$rawInput = file_get_contents("php://input");
$data = json_decode($rawInput, true);

if (!$data) {
    $data = $_POST;
}

$email = isset($data['email']) ? trim($data['email']) : null;
$username = isset($data['username']) ? trim($data['username']) : null;
$password = isset($data['password']) ? trim($data['password']) : null;
$firstName = isset($data['first_name']) ? trim($data['first_name']) : '';
$lastName = isset($data['last_name']) ? trim($data['last_name']) : '';
$phoneNumber = isset($data['phone_number']) ? trim($data['phone_number']) : '';

// Validation
if (empty($username) || empty($password)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Username and password are required."]);
    exit();
}

if (strlen($password) < 6) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Password must be at least 6 characters."]);
    exit();
}

try {
    // Check if user already exists
    $checkSql = "SELECT id FROM users WHERE username = :username OR (email IS NOT NULL AND email = :email) LIMIT 1";
    $stmt = $db->prepare($checkSql);
    $stmt->execute([
        ':username' => $username,
        ':email' => $email
    ]);

    if ($stmt->fetch()) {
        http_response_code(409);
        echo json_encode(["success" => false, "message" => "Username or Email is already registered."]);
        exit();
    }

    // Hash password
    $passwordHash = password_hash($password, PASSWORD_BCRYPT);

    // Insert user
    $insertSql = "INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number) 
                  VALUES (:email, :username, :password_hash, :first_name, :last_name, :phone_number) 
                  RETURNING id, email, username, first_name, last_name, phone_number, created_at";
    
    $insertStmt = $db->prepare($insertSql);
    $insertStmt->execute([
        ':email' => $email,
        ':username' => $username,
        ':password_hash' => $passwordHash,
        ':first_name' => $firstName,
        ':last_name' => $lastName,
        ':phone_number' => $phoneNumber
    ]);

    $newUser = $insertStmt->fetch();

    http_response_code(201);
    echo json_encode([
        "success" => true,
        "message" => "Registration successful.",
        "user" => $newUser
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Server error: " . $e->getMessage()
    ]);
}
