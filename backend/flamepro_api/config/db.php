<?php
// backend/flamepro_api/config/db.php

// Handle preflight OPTIONS request if in an API context
if (isset($_SERVER['REQUEST_METHOD']) && $_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With');
    http_response_code(200);
    exit();
}

class Database {
    // Modify these credentials to match your PostgreSQL configuration in pgAdmin 4
    private $host = "localhost";
    private $port = "5432";
    private $db_name = "flamepro_db";
    private $username = "postgres";
    private $password = "root"; // CHANGE THIS if your PostgreSQL superuser password differs
    private $conn;

    public function getConnection() {
        $this->conn = null;

        try {
            $dsn = "pgsql:host=" . $this->host . ";port=" . $this->port . ";dbname=" . $this->db_name;
            $this->conn = new PDO($dsn, $this->username, $this->password, [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false,
            ]);
        } catch (PDOException $exception) {
            http_response_code(500);
            echo json_encode([
                "success" => false,
                "message" => "Database connection error: " . $exception->getMessage()
            ]);
            exit();
        }

        return $this->conn;
    }
}
