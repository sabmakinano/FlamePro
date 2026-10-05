<?php
// backend/flamepro_api/portal/index.php
session_start();
header('Content-Type: text/html; charset=UTF-8');
require_once __DIR__ . '/../config/db.php';

$error = '';
$database = new Database();
$db = $database->getConnection();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $usernameOrEmail = trim($_POST['username_or_email'] ?? '');
    $password = trim($_POST['password'] ?? '');

    if (empty($usernameOrEmail) || empty($password)) {
        $error = 'Please enter your username/email and password.';
    } else {
        $stmt = $db->prepare("SELECT * FROM users WHERE LOWER(username) = LOWER(:input) OR LOWER(email) = LOWER(:input) LIMIT 1");
        $stmt->execute([':input' => $usernameOrEmail]);
        $user = $stmt->fetch();

        if ($user && password_verify($password, $user['password_hash'])) {
            if (isset($user['is_active']) && !$user['is_active']) {
                $error = 'This account has been deactivated by the Administrator.';
            } else if (in_array($user['role'], ['admin', 'sales'])) {
                $_SESSION['portal_user'] = $user;
                header('Location: dashboard.php');
                exit();
            } else {
                $error = 'Access restricted. Only Sales and Administrator accounts can access this portal.';
            }
        } else {
            $error = 'Invalid username/email or password.';
        }
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FlamePro • Staff & Admin Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --brand-red: #E50914;
            --brand-dark-red: #D32F2F;
            --text-dark: #212121;
            --bg-light: #F8F9FA;
        }
        body {
            font-family: 'Poppins', sans-serif;
            background-color: var(--bg-light);
            color: var(--text-dark);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .login-card {
            background: #FFFFFF;
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(229, 9, 20, 0.08), 0 4px 12px rgba(0, 0, 0, 0.04);
            max-width: 440px;
            width: 100%;
            padding: 40px 32px;
            border: 1px solid rgba(0, 0, 0, 0.05);
        }
        .brand-logo {
            width: 72px;
            height: 72px;
            object-fit: contain;
            filter: drop-shadow(0 4px 10px rgba(229, 9, 20, 0.25));
        }
        .brand-title {
            font-weight: 700;
            font-size: 26px;
            color: var(--text-dark);
            letter-spacing: -0.5px;
            margin-top: 12px;
        }
        .brand-title span {
            color: var(--brand-red);
        }
        .brand-subtitle {
            font-size: 13px;
            color: #757575;
            margin-bottom: 28px;
        }
        .form-label {
            font-size: 13px;
            font-weight: 600;
            color: #424242;
            margin-bottom: 6px;
        }
        .form-control {
            border-radius: 12px;
            padding: 12px 16px;
            border: 1.5px solid #E0E0E0;
            font-size: 14px;
            color: #000000 !important;
            background-color: #FFFFFF !important;
            transition: all 0.2s ease;
        }
        .form-control::placeholder {
            color: #9E9E9E !important;
        }
        .form-control:focus {
            border-color: var(--brand-red);
            color: #000000 !important;
            background-color: #FFFFFF !important;
            box-shadow: 0 0 0 4px rgba(229, 9, 20, 0.12);
        }
        .btn-brand {
            background-color: var(--brand-red);
            color: #FFFFFF;
            font-weight: 600;
            padding: 13px;
            border-radius: 12px;
            font-size: 15px;
            border: none;
            transition: all 0.2s ease;
            box-shadow: 0 4px 14px rgba(229, 9, 20, 0.35);
        }
        .btn-brand:hover {
            background-color: var(--brand-dark-red);
            color: #FFFFFF;
            transform: translateY(-1px);
        }
    </style>
</head>
<body>

<div class="login-card text-center">
    <img src="assets/logo.png" alt="FlamePro Logo" class="brand-logo" onerror="this.src='https://cdn-icons-png.flaticon.com/512/785/785116.png'">
    <div class="brand-title">Flame<span>Pro</span></div>
    <div class="brand-subtitle">Management & Sales Portal</div>

    <?php if (!empty($error)): ?>
        <div class="alert alert-danger py-2 text-start small mb-3" role="alert">
            <i class="fa-solid fa-circle-exclamation me-1"></i> <?= htmlspecialchars($error) ?>
        </div>
    <?php endif; ?>

    <form method="POST" action="" class="text-start">
        <div class="mb-3">
            <label class="form-label" for="usernameOrEmail">Username or Email</label>
            <div class="input-group">
                <input type="text" class="form-control" id="usernameOrEmail" name="username_or_email" placeholder="admin or sales" required autofocus>
            </div>
        </div>

        <div class="mb-4">
            <label class="form-label" for="password">Password</label>
            <input type="password" class="form-control" id="password" name="password" placeholder="••••••••" required>
        </div>

        <button type="submit" class="btn btn-brand w-100 mb-2">
            <i class="fa-solid fa-right-to-bracket me-2"></i> Log In to Portal
        </button>
    </form>

    <div class="mt-4 text-muted small" style="font-size: 11.5px;">
        &copy; <?= date('Y') ?> FlamePro Safety Solutions. All rights reserved.
    </div>
</div>

</body>
</html>
