<?php
// Audit logging helper for FlamePro admin portal
// Ensure session is started for user tracking
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}
/**
 * Write an audit log entry to the database.
 *
 * @param PDO   $pdo    Active PDO connection
 * @param string $action Short description of the action (e.g., 'order_remittance')
 * @param array  $context Optional additional data (e.g., order id, amount)
 * @return bool          True on success, false on failure
 */
function write_audit_log(PDO $pdo, string $action, array $context = []): bool {
    try {
        $userId = $_SESSION['user_id'] ?? 'guest';
        $ip = $_SERVER['HTTP_CLIENT_IP'] ?? $_SERVER['HTTP_X_FORWARDED_FOR'] ?? $_SERVER['REMOTE_ADDR'] ?? 'UNKNOWN';
        $contextJson = $context ? json_encode($context) : null;
        $stmt = $pdo->prepare("INSERT INTO audit_logs (user_id, action, context, ip_address) VALUES (:user_id, :action, :context, :ip)");
        return $stmt->execute([
            ':user_id' => $userId,
            ':action'  => $action,
            ':context' => $contextJson,
            ':ip'      => $ip
        ]);
    } catch (Throwable $e) {
        error_log('Audit log error: ' . $e->getMessage());
        return false;
    }
}
function logAudit(PDO $pdo, int $userId, string $username, string $actionDetail, array $context = []): bool {
    // Ensure session has correct user data for write_audit_log
    if (session_status() === PHP_SESSION_NONE) {
        session_start();
    }
    // Preserve original session values
    $prevUserId = $_SESSION['user_id'] ?? null;
    $prevUsername = $_SESSION['username'] ?? null;
    $_SESSION['user_id'] = $userId;
    $_SESSION['username'] = $username;
    $result = write_audit_log($pdo, $actionDetail, $context);
    // Restore previous session values
    if ($prevUserId !== null) {
        $_SESSION['user_id'] = $prevUserId;
    } else {
        unset($_SESSION['user_id']);
    }
    if ($prevUsername !== null) {
        $_SESSION['username'] = $prevUsername;
    } else {
        unset($_SESSION['username']);
    }
    return $result;
}
?>
