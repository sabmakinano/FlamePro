<?php
// backend/flamepro_api/portal/dashboard.php
session_start();
header('Content-Type: text/html; charset=UTF-8');
require_once __DIR__ . '/../config/db.php';

if (!isset($_SESSION['portal_user'])) {
    header('Location: index.php');
    exit();
}

$currentUser = $_SESSION['portal_user'];
$database = new Database();
$db = $database->getConnection();

$message = '';
$messageType = 'success';

// Handle Logout
if (isset($_GET['action']) && $_GET['action'] === 'logout') {
    session_destroy();
    header('Location: index.php');
    exit();
}

// --------------------------------------------------------------------------
// POST ACTIONS
// --------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    // 1. Assign Delivery Rider
    if ($action === 'assign_rider') {
        $orderId = intval($_POST['order_id'] ?? 0);
        $riderId = intval($_POST['rider_id'] ?? 0);
        if ($orderId > 0 && $riderId > 0) {
            $stmt = $db->prepare("UPDATE orders SET assigned_delivery_id = :rider_id, status = 'Out for Delivery', delivery_status = 'Assigned' WHERE id = :order_id");
            $stmt->execute([':rider_id' => $riderId, ':order_id' => $orderId]);
            $message = "Delivery Rider assigned to Order #$orderId successfully!";
        }
    }

    // 2. Update Order Status (Approve / Deliver / Cancel)
    if ($action === 'update_order_status') {
        $orderId = intval($_POST['order_id'] ?? 0);
        $newStatus = trim($_POST['status'] ?? '');
        if ($orderId > 0 && !empty($newStatus)) {
            $stmt = $db->prepare("UPDATE orders SET status = :status WHERE id = :order_id");
            $stmt->execute([':status' => $newStatus, ':order_id' => $orderId]);
            $message = "Order #$orderId updated to $newStatus.";
        }
    }

    // 3. Assign Technician to Service Request
    if ($action === 'assign_technician') {
        $serviceId = intval($_POST['service_id'] ?? 0);
        $techId = intval($_POST['tech_id'] ?? 0);
        if ($serviceId > 0 && $techId > 0) {
            $stmt = $db->prepare("UPDATE service_requests SET assigned_technician_id = :tech_id, status = 'Assigned' WHERE id = :service_id");
            $stmt->execute([':tech_id' => $techId, ':service_id' => $serviceId]);
            $message = "Technician assigned to Service Request #$serviceId.";
        }
    }

    // 4. Update Service Status (Complete / Cancel)
    if ($action === 'update_service_status') {
        $serviceId = intval($_POST['service_id'] ?? 0);
        $newStatus = trim($_POST['status'] ?? '');
        if ($serviceId > 0 && !empty($newStatus)) {
            $stmt = $db->prepare("UPDATE service_requests SET status = :status WHERE id = :service_id");
            $stmt->execute([':status' => $newStatus, ':service_id' => $serviceId]);
            $message = "Service Request #$serviceId marked as $newStatus.";
        }
    }

    // 5. Admin Only: Add New Staff Member
    if ($action === 'add_staff' && $currentUser['role'] === 'admin') {
        $name = trim($_POST['staff_name'] ?? '');
        $username = trim($_POST['staff_username'] ?? '');
        $email = trim($_POST['staff_email'] ?? '');
        $password = trim($_POST['staff_password'] ?? '');
        $role = trim($_POST['staff_role'] ?? 'sales');
        $phone = trim($_POST['staff_phone'] ?? '');

        if (!empty($username) && !empty($password)) {
            $check = $db->prepare("SELECT id FROM users WHERE username = :u OR email = :e");
            $check->execute([':u' => $username, ':e' => $email]);
            if ($check->fetch()) {
                $message = "Username or Email is already in use.";
                $messageType = 'danger';
            } else {
                $hash = password_hash($password, PASSWORD_BCRYPT);
                $ins = $db->prepare("INSERT INTO users (first_name, username, email, password_hash, role, phone_number, is_active) VALUES (:fn, :u, :e, :p, :r, :ph, TRUE)");
                $ins->execute([
                    ':fn' => $name,
                    ':u' => $username,
                    ':e' => $email,
                    ':p' => $hash,
                    ':r' => $role,
                    ':ph' => $phone
                ]);
                $message = "New staff member '$name' created successfully!";
            }
        }
    }

    // 6. Admin Only: Toggle Staff Active / Deactivated Status
    if ($action === 'toggle_staff' && $currentUser['role'] === 'admin') {
        $staffId = intval($_POST['staff_id'] ?? 0);
        if ($staffId > 0 && $staffId !== $currentUser['id']) {
            $stmt = $db->prepare("UPDATE users SET is_active = NOT is_active WHERE id = :id");
            $stmt->execute([':id' => $staffId]);
            $message = "Staff account status toggled successfully.";
        }
    }

    // 7. Admin Only: Delete Staff Account Permanently
    if ($action === 'delete_staff' && $currentUser['role'] === 'admin') {
        $staffId = intval($_POST['staff_id'] ?? 0);
        if ($staffId > 0 && $staffId !== $currentUser['id']) {
            $stmt = $db->prepare("DELETE FROM users WHERE id = :id AND role != 'customer'");
            $stmt->execute([':id' => $staffId]);
            $message = "Staff account deleted successfully.";
        }
    }
}

// --------------------------------------------------------------------------
// ANALYTICS & FILTERING LOGIC
// --------------------------------------------------------------------------
$selectedYear = isset($_GET['year']) ? intval($_GET['year']) : intval(date('Y'));
$selectedMonth = isset($_GET['month']) ? intval($_GET['month']) : 0; // 0 = All Months

$monthsList = [
    1 => 'January', 2 => 'February', 3 => 'March', 4 => 'April',
    5 => 'May', 6 => 'June', 7 => 'July', 8 => 'August',
    9 => 'September', 10 => 'October', 11 => 'November', 12 => 'December'
];

// Query Available Years from Database
$yearsQuery = $db->query("SELECT DISTINCT EXTRACT(YEAR FROM created_at)::int AS yr FROM orders UNION SELECT EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::int ORDER BY yr DESC");
$availableYears = $yearsQuery->fetchAll(PDO::FETCH_COLUMN) ?: [intval(date('Y'))];

// Build Date Filter Condition
$filterCondition = "EXTRACT(YEAR FROM o.created_at) = :year";
$filterParams = [':year' => $selectedYear];

if ($selectedMonth > 0) {
    $filterCondition .= " AND EXTRACT(MONTH FROM o.created_at) = :month";
    $filterParams[':month'] = $selectedMonth;
}

// 1. Total Filtered Revenue & Orders (only Delivered = real collected sales)
$kpiStmt = $db->prepare("SELECT COUNT(*) AS period_orders, 
                                COALESCE(SUM(CAST(REGEXP_REPLACE(o.total_price, '[^0-9.]', '', 'g') AS NUMERIC)), 0) AS period_revenue 
                         FROM orders o 
                         WHERE $filterCondition AND o.status = 'Delivered'");
$kpiStmt->execute($filterParams);
$kpiData = $kpiStmt->fetch();
$periodOrders = intval($kpiData['period_orders'] ?? 0);
$periodRevenue = floatval($kpiData['period_revenue'] ?? 0);
$avgOrderValue = $periodOrders > 0 ? ($periodRevenue / $periodOrders) : 0;

// 2. Sales by Category (for Pie Chart)
$catStmt = $db->prepare("SELECT COALESCE(p.category_tag, 'Fire Extinguishers') AS category,
                                SUM(oi.quantity * CAST(REGEXP_REPLACE(oi.unit_price, '[^0-9.]', '', 'g') AS NUMERIC)) AS total_sales,
                                SUM(oi.quantity) AS total_qty
                         FROM orders o
                         JOIN order_items oi ON o.id = oi.order_id
                         LEFT JOIN products p ON oi.product_id = p.id OR LOWER(oi.product_name) = LOWER(p.name)
                         WHERE $filterCondition
                         GROUP BY category
                         ORDER BY total_sales DESC");
$catStmt->execute($filterParams);
$categorySales = $catStmt->fetchAll();

// Default pie chart categories if empty
$chartLabels = [];
$chartValues = [];
$chartColors = ['#E50914', '#1E88E5', '#FB8C00', '#43A047', '#8E24AA', '#00ACC1'];

if (!empty($categorySales)) {
    foreach ($categorySales as $cs) {
        $chartLabels[] = $cs['category'];
        $chartValues[] = floatval($cs['total_sales']);
    }
} else {
    // Demo placeholder categories if no sales in this specific filtered month yet
    $chartLabels = ['Fire Extinguishers', 'Fire Sprinklers', 'Fire Hose', 'Fireman Equipments', 'Others'];
    $chartValues = [0, 0, 0, 0, 0];
}

// 3. Monthly Sales for Bar Chart across the selected Year
$monthlyStmt = $db->prepare("SELECT EXTRACT(MONTH FROM created_at)::int AS m_num,
                                    COALESCE(SUM(CAST(REGEXP_REPLACE(total_price, '[^0-9.]', '', 'g') AS NUMERIC)), 0) AS m_total
                             FROM orders
                             WHERE EXTRACT(YEAR FROM created_at) = :year
                             GROUP BY m_num
                             ORDER BY m_num ASC");
$monthlyStmt->execute([':year' => $selectedYear]);
$monthlyResults = $monthlyStmt->fetchAll();

$monthLabels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
$monthlyChartData = array_fill(0, 12, 0.0);
foreach ($monthlyResults as $mr) {
    $idx = intval($mr['m_num']) - 1;
    if ($idx >= 0 && $idx < 12) {
        $monthlyChartData[$idx] = floatval($mr['m_total']);
    }
}

// --------------------------------------------------------------------------
// OTHER DATA FETCHING
// --------------------------------------------------------------------------
$totalOrdersCount = $db->query("SELECT COUNT(*) FROM orders")->fetchColumn() ?: 0;
$pendingOrdersCount = $db->query("SELECT COUNT(*) FROM orders WHERE status IN ('Pending', 'Confirmed')")->fetchColumn() ?: 0;
$activeStaffCount = $db->query("SELECT COUNT(*) FROM users WHERE role IN ('sales', 'technician', 'delivery') AND is_active = TRUE")->fetchColumn() ?: 0;
$pendingServicesCount = $db->query("SELECT COUNT(*) FROM service_requests WHERE status = 'Pending Approval'")->fetchColumn() ?: 0;

$ordersQuery = "SELECT o.*, u.first_name, u.last_name, u.phone_number AS customer_phone,
                       r.first_name AS rider_first_name, r.last_name AS rider_last_name
                FROM orders o
                LEFT JOIN users u ON o.user_id = u.id
                LEFT JOIN users r ON o.assigned_delivery_id = r.id
                ORDER BY o.created_at DESC";
$orders = $db->query($ordersQuery)->fetchAll();

$orderItemsStmt = $db->prepare("SELECT * FROM order_items WHERE order_id = :order_id");
foreach ($orders as &$ord) {
    $orderItemsStmt->execute([':order_id' => $ord['id']]);
    $ord['items'] = $orderItemsStmt->fetchAll();
}

$servicesQuery = "SELECT s.*, u.first_name AS cust_first, u.last_name AS cust_last, u.phone_number AS cust_phone,
                         t.first_name AS tech_first, t.last_name AS tech_last
                  FROM service_requests s
                  LEFT JOIN users u ON s.user_id = u.id
                  LEFT JOIN users t ON s.assigned_technician_id = t.id
                  ORDER BY s.created_at DESC";
$services = $db->query($servicesQuery)->fetchAll();

$riders = $db->query("SELECT id, first_name, last_name, phone_number FROM users WHERE role = 'delivery' AND is_active = TRUE")->fetchAll();
$technicians = $db->query("SELECT id, first_name, last_name, phone_number FROM users WHERE role = 'technician' AND is_active = TRUE")->fetchAll();
$allStaff = $db->query("SELECT id, first_name, last_name, username, email, role, phone_number, is_active, created_at FROM users WHERE role IN ('sales', 'technician', 'delivery', 'admin') ORDER BY role ASC, id ASC")->fetchAll();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FlamePro • <?= strtoupper($currentUser['role']) ?> Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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
        }
        .navbar-brand-custom {
            background-color: #FFFFFF;
            border-bottom: 2px solid rgba(229, 9, 20, 0.1);
            box-shadow: 0 4px 14px rgba(0,0,0,0.03);
            padding: 12px 24px;
        }
        .brand-logo-nav {
            width: 44px;
            height: 44px;
            object-fit: contain;
            margin-right: 12px;
        }
        .brand-text {
            font-weight: 700;
            font-size: 22px;
            color: var(--text-dark);
            letter-spacing: -0.5px;
        }
        .brand-text span {
            color: var(--brand-red);
        }
        .role-badge {
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.5px;
            padding: 5px 12px;
            border-radius: 20px;
            text-transform: uppercase;
        }
        .role-admin { background: #FFEAEA; color: var(--brand-red); border: 1px solid var(--brand-red); }
        .role-sales { background: #E8F0FE; color: #1A73E8; border: 1px solid #1A73E8; }

        .stat-card {
            background: #FFFFFF;
            border-radius: 16px;
            padding: 22px 20px;
            border: 1px solid rgba(0,0,0,0.05);
            box-shadow: 0 4px 16px rgba(0,0,0,0.03);
            transition: transform 0.2s ease;
        }
        .stat-card:hover {
            transform: translateY(-2px);
        }
        .stat-icon {
            width: 52px;
            height: 52px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
        }
        .icon-red { background: #FFEAEA; color: var(--brand-red); }
        .icon-blue { background: #E8F0FE; color: #1A73E8; }
        .icon-amber { background: #FFF4E5; color: #E65100; }
        .icon-green { background: #E6F4EA; color: #137333; }

        .content-card {
            background: #FFFFFF;
            border-radius: 18px;
            padding: 24px;
            border: 1px solid rgba(0,0,0,0.06);
            box-shadow: 0 4px 20px rgba(0,0,0,0.03);
            margin-bottom: 30px;
        }
        .nav-pills .nav-link {
            border-radius: 12px;
            font-weight: 600;
            color: #616161;
            padding: 10px 18px;
            margin-right: 8px;
            font-size: 14px;
            transition: all 0.2s;
        }
        .nav-pills .nav-link.active {
            background-color: var(--brand-red);
            color: #FFFFFF;
            box-shadow: 0 4px 12px rgba(229, 9, 20, 0.3);
        }
        .table > :not(caption) > * > * {
            padding: 14px 14px;
            vertical-align: middle;
            font-size: 13.5px;
        }
        .badge-status {
            font-size: 11.5px;
            font-weight: 600;
            padding: 5px 10px;
            border-radius: 8px;
        }
        .btn-brand-sm {
            background-color: var(--brand-red);
            color: white;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 600;
            padding: 6px 12px;
            border: none;
        }
        .btn-brand-sm:hover {
            background-color: var(--brand-dark-red);
            color: white;
        }
        .filter-bar {
            background: #FFF5F5;
            border: 1px solid #FFE0E0;
            border-radius: 14px;
            padding: 16px 20px;
        }
    </style>
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-brand-custom sticky-top">
    <div class="container-fluid">
        <div class="d-flex align-items-center">
            <img src="assets/logo.png" alt="FlamePro Logo" class="brand-logo-nav" onerror="this.src='https://cdn-icons-png.flaticon.com/512/785/785116.png'">
            <div>
                <div class="brand-text">Flame<span>Pro</span></div>
                <small class="text-muted" style="font-size: 11px;">Official Operations & Sales Portal</small>
            </div>
        </div>

        <div class="d-flex align-items-center gap-3">
            <span class="role-badge <?= $currentUser['role'] === 'admin' ? 'role-admin' : 'role-sales' ?>">
                <i class="fa-solid <?= $currentUser['role'] === 'admin' ? 'fa-shield-halved' : 'fa-headset' ?> me-1"></i>
                <?= strtoupper($currentUser['role']) ?>
            </span>
            <div class="text-end d-none d-sm-block">
                <div class="fw-bold small"><?= htmlspecialchars($currentUser['first_name'] . ' ' . $currentUser['last_name']) ?></div>
                <div class="text-muted" style="font-size: 11px;">@<?= htmlspecialchars($currentUser['username']) ?></div>
            </div>
            <a href="?action=logout" class="btn btn-outline-danger btn-sm rounded-pill px-3 fw-semibold">
                <i class="fa-solid fa-right-from-bracket me-1"></i> Logout
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid py-4 px-md-5">

    <?php if (!empty($message)): ?>
        <div class="alert alert-<?= $messageType ?> alert-dismissible fade show rounded-4 py-2 small" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i> <?= htmlspecialchars($message) ?>
            <button type="button" class="btn-close py-2" data-bs-dismiss="alert"></button>
        </div>
    <?php endif; ?>

    <!-- 4 Stats Cards -->
    <div class="row g-3 mb-4">
        <div class="col-6 col-md-3">
            <div class="stat-card d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small fw-semibold">Total Orders</div>
                    <div class="fs-3 fw-bold mt-1"><?= $totalOrdersCount ?></div>
                </div>
                <div class="stat-icon icon-red">
                    <i class="fa-solid fa-fire-extinguisher"></i>
                </div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small fw-semibold">Pending Approvals</div>
                    <div class="fs-3 fw-bold mt-1 text-warning"><?= $pendingOrdersCount ?></div>
                </div>
                <div class="stat-icon icon-amber">
                    <i class="fa-solid fa-clock"></i>
                </div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small fw-semibold">Service Requests</div>
                    <div class="fs-3 fw-bold mt-1 text-primary"><?= $pendingServicesCount ?></div>
                </div>
                <div class="stat-icon icon-blue">
                    <i class="fa-solid fa-wrench"></i>
                </div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small fw-semibold">Active Staff</div>
                    <div class="fs-3 fw-bold mt-1 text-success"><?= $activeStaffCount ?></div>
                </div>
                <div class="stat-icon icon-green">
                    <i class="fa-solid fa-users"></i>
                </div>
            </div>
        </div>
    </div>

    <!-- Main Navigation Tabs -->
    <div class="content-card">
        <ul class="nav nav-pills mb-4" id="portalTabs" role="tablist">
            <li class="nav-item">
                <button class="nav-link active" id="analytics-tab" data-bs-toggle="pill" data-bs-target="#analytics-pane">
                    <i class="fa-solid fa-chart-pie me-1"></i> Sales & Monthly Analytics
                </button>
            </li>
            <li class="nav-item">
                <button class="nav-link" id="orders-tab" data-bs-toggle="pill" data-bs-target="#orders-pane">
                    <i class="fa-solid fa-box me-1"></i> Customer Product Orders (<?= count($orders) ?>)
                </button>
            </li>
            <li class="nav-item">
                <button class="nav-link" id="services-tab" data-bs-toggle="pill" data-bs-target="#services-pane">
                    <i class="fa-solid fa-screwdriver-wrench me-1"></i> Services (Refill & Inspect) (<?= count($services) ?>)
                </button>
            </li>
            <?php if ($currentUser['role'] === 'admin'): ?>
            <li class="nav-item">
                <button class="nav-link" id="staff-tab" data-bs-toggle="pill" data-bs-target="#staff-pane">
                    <i class="fa-solid fa-user-gear me-1"></i> Staff Management (<?= count($allStaff) ?>)
                </button>
            </li>
            <?php endif; ?>
        </ul>

        <div class="tab-content" id="portalTabsContent">

            <!-- TAB 1: SALES & MONTHLY ANALYTICS (PIE & BAR CHARTS) -->
            <div class="tab-pane fade show active" id="analytics-pane">
                
                <!-- Filter Bar (Month & Year Selector) -->
                <div class="filter-bar mb-4">
                    <form method="GET" class="row g-3 align-items-center">
                        <div class="col-12 col-md-4">
                            <h6 class="fw-bold mb-1 text-danger">
                                <i class="fa-solid fa-sliders me-1"></i> Sales Analytics Filter
                            </h6>
                            <small class="text-muted">Select Year and Month to view category breakdowns.</small>
                        </div>
                        <div class="col-6 col-md-3">
                            <label class="form-label small fw-semibold text-dark mb-1">Select Year</label>
                            <select name="year" class="form-select form-select-sm rounded-3">
                                <?php foreach ($availableYears as $yr): ?>
                                    <option value="<?= $yr ?>" <?= ($selectedYear == $yr) ? 'selected' : '' ?>>
                                        <?= $yr ?>
                                    </option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-6 col-md-3">
                            <label class="form-label small fw-semibold text-dark mb-1">Select Month</label>
                            <select name="month" class="form-select form-select-sm rounded-3">
                                <option value="0" <?= ($selectedMonth == 0) ? 'selected' : '' ?>>All Months (Full Year)</option>
                                <?php foreach ($monthsList as $num => $name): ?>
                                    <option value="<?= $num ?>" <?= ($selectedMonth == $num) ? 'selected' : '' ?>>
                                        <?= $name ?>
                                    </option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-12 col-md-2 d-flex align-items-end">
                            <button type="submit" class="btn btn-danger btn-sm w-100 rounded-3 py-2 fw-semibold" style="background: #E50914;">
                                <i class="fa-solid fa-filter me-1"></i> Apply Filter
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Filtered Period Summary KPIs -->
                <div class="row g-3 mb-4">
                    <div class="col-12 col-md-4">
                        <div class="p-3 border rounded-4 bg-light text-center">
                            <small class="text-muted fw-semibold">Filtered Period Total Revenue</small>
                            <div class="fs-4 fw-bold text-danger mt-1">₱ <?= number_format($periodRevenue, 2) ?></div>
                            <small class="text-secondary"><?= ($selectedMonth > 0 ? $monthsList[$selectedMonth] : 'All Months') . ' ' . $selectedYear ?></small>
                        </div>
                    </div>
                    <div class="col-12 col-md-4">
                        <div class="p-3 border rounded-4 bg-light text-center">
                            <small class="text-muted fw-semibold">Filtered Period Orders</small>
                            <div class="fs-4 fw-bold text-primary mt-1"><?= $periodOrders ?> Orders</div>
                            <small class="text-secondary">Cash on Delivery</small>
                        </div>
                    </div>
                    <div class="col-12 col-md-4">
                        <div class="p-3 border rounded-4 bg-light text-center">
                            <small class="text-muted fw-semibold">Average Order Value</small>
                            <div class="fs-4 fw-bold text-success mt-1">₱ <?= number_format($avgOrderValue, 2) ?></div>
                            <small class="text-secondary">Per customer checkout</small>
                        </div>
                    </div>
                </div>

                <!-- Charts Row: Pie Chart & Bar Chart -->
                <div class="row g-4 mb-4">
                    
                    <!-- PIE / DOUGHNUT CHART -->
                    <div class="col-12 col-lg-5">
                        <div class="p-4 border rounded-4 bg-white shadow-sm h-100">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <div>
                                    <h6 class="fw-bold mb-0">Sales by Product Category</h6>
                                    <small class="text-muted">
                                        <?= ($selectedMonth > 0 ? $monthsList[$selectedMonth] : 'Full Year') . ' ' . $selectedYear ?>
                                    </small>
                                </div>
                                <span class="badge bg-danger rounded-pill px-3 py-1">Pie Breakdown</span>
                            </div>
                            <div style="position: relative; height: 280px;">
                                <canvas id="categoryPieChart"></canvas>
                            </div>
                        </div>
                    </div>

                    <!-- MONTHLY SALES BAR / LINE CHART -->
                    <div class="col-12 col-lg-7">
                        <div class="p-4 border rounded-4 bg-white shadow-sm h-100">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <div>
                                    <h6 class="fw-bold mb-0">Monthly Revenue Trend (<?= $selectedYear ?>)</h6>
                                    <small class="text-muted">Jan - Dec Revenue Overview</small>
                                </div>
                                <span class="badge bg-primary rounded-pill px-3 py-1">Annual Overview</span>
                            </div>
                            <div style="position: relative; height: 280px;">
                                <canvas id="monthlyBarChart"></canvas>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- Category Breakdown Table -->
                <div class="table-responsive border rounded-4">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>Category Name</th>
                                <th>Quantity Sold</th>
                                <th>Total Revenue (₱)</th>
                                <th>Share of Sales</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php if (empty($categorySales)): ?>
                                <tr>
                                    <td colspan="4" class="text-center py-4 text-muted">
                                        No sales records found for <?= ($selectedMonth > 0 ? $monthsList[$selectedMonth] : 'Year') . ' ' . $selectedYear ?>.
                                    </td>
                                </tr>
                            <?php else: ?>
                                <?php foreach ($categorySales as $cat): 
                                    $pct = $periodRevenue > 0 ? (($cat['total_sales'] / $periodRevenue) * 100) : 0;
                                ?>
                                <tr>
                                    <td class="fw-bold text-dark">
                                        <i class="fa-solid fa-tag text-danger me-2"></i><?= htmlspecialchars($cat['category']) ?>
                                    </td>
                                    <td><span class="badge bg-light text-dark border px-2 py-1"><?= intval($cat['total_qty']) ?> units</span></td>
                                    <td class="fw-bold text-danger">₱ <?= number_format($cat['total_sales'], 2) ?></td>
                                    <td style="width: 200px;">
                                        <div class="d-flex align-items-center gap-2">
                                            <div class="progress flex-grow-1" style="height: 8px;">
                                                <div class="progress-bar bg-danger" role="progressbar" style="width: <?= round($pct, 1) ?>%;"></div>
                                            </div>
                                            <span class="small fw-semibold"><?= round($pct, 1) ?>%</span>
                                        </div>
                                    </td>
                                </tr>
                                <?php endforeach; ?>
                            <?php endif; ?>
                        </tbody>
                    </table>
                </div>

            </div>

            <!-- TAB 2: PRODUCT ORDERS -->
            <div class="tab-pane fade" id="orders-pane">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0">Customer Orders & Delivery Dispatch</h5>
                    <small class="text-muted"><i class="fa-solid fa-circle-info me-1"></i> Sales approves orders and assigns delivery riders.</small>
                </div>

                <!-- Active vs Delivered sub-tabs -->
                <?php
                    $activeOrders    = array_filter($orders, fn($o) => !in_array($o['status'], ['Delivered', 'Cancelled']));
                    $deliveredOrders = array_filter($orders, fn($o) => $o['status'] === 'Delivered');
                    $cancelledOrders = array_filter($orders, fn($o) => $o['status'] === 'Cancelled');
                ?>

                <ul class="nav nav-tabs mb-3" id="orderSubTabs">
                    <li class="nav-item">
                        <button class="nav-link active fw-semibold" data-bs-toggle="tab" data-bs-target="#activeOrdersPane">
                            <i class="fa-solid fa-hourglass-half me-1 text-warning"></i>
                            Active Orders <span class="badge bg-warning text-dark ms-1"><?= count($activeOrders) ?></span>
                        </button>
                    </li>
                    <li class="nav-item">
                        <button class="nav-link fw-semibold" data-bs-toggle="tab" data-bs-target="#deliveredOrdersPane">
                            <i class="fa-solid fa-circle-check me-1 text-success"></i>
                            Delivered <span class="badge bg-success ms-1"><?= count($deliveredOrders) ?></span>
                        </button>
                    </li>
                    <li class="nav-item">
                        <button class="nav-link fw-semibold" data-bs-toggle="tab" data-bs-target="#cancelledOrdersPane">
                            <i class="fa-solid fa-ban me-1 text-danger"></i>
                            Cancelled <span class="badge bg-danger ms-1"><?= count($cancelledOrders) ?></span>
                        </button>
                    </li>
                </ul>

                <div class="tab-content">

                    <!-- Active Orders -->
                    <div class="tab-pane fade show active" id="activeOrdersPane">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Order #</th>
                                        <th>Customer & Address</th>
                                        <th>Items Ordered</th>
                                        <th>Total (COD)</th>
                                        <th>Status</th>
                                        <th>Assign Rider</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <?php if (empty($activeOrders)): ?>
                                    <tr>
                                        <td colspan="7" class="text-center py-5 text-muted">
                                            <i class="fa-solid fa-check-double fa-3x mb-3 text-success d-block"></i>
                                            All orders have been delivered or there are no active orders!
                                        </td>
                                    </tr>
                                <?php else: ?>
                                    <?php foreach ($activeOrders as $ord): ?>
                                    <tr>
                                        <td>
                                            <span class="fw-bold text-danger"><?= htmlspecialchars($ord['order_number']) ?></span>
                                            <div class="text-muted" style="font-size: 11px;"><?= date('M d, Y h:i A', strtotime($ord['created_at'])) ?></div>
                                        </td>
                                        <td>
                                            <div class="fw-semibold"><?= htmlspecialchars($ord['first_name'] ? ($ord['first_name'].' '.$ord['last_name']) : 'Guest Customer') ?></div>
                                            <div class="text-muted small"><i class="fa-solid fa-location-dot text-danger me-1"></i><?= htmlspecialchars($ord['shipping_address'] ?: 'Customer Address') ?></div>
                                        </td>
                                        <td>
                                            <?php if (!empty($ord['items'])): ?>
                                                <ul class="list-unstyled mb-0 small">
                                                    <?php foreach ($ord['items'] as $item): ?>
                                                        <li>• <?= htmlspecialchars($item['product_name']) ?> <span class="badge bg-light text-dark">x<?= $item['quantity'] ?></span></li>
                                                    <?php endforeach; ?>
                                                </ul>
                                            <?php else: ?>
                                                <span class="small text-muted">Items included</span>
                                            <?php endif; ?>
                                        </td>
                                        <td>
                                            <div class="fw-bold text-danger"><?= htmlspecialchars($ord['total_price']) ?></div>
                                            <span class="badge bg-light text-secondary border">Cash on Delivery</span>
                                        </td>
                                        <td>
                                            <?php
                                                $statusClass = 'bg-warning text-dark';
                                                if ($ord['status'] === 'Confirmed') $statusClass = 'bg-primary';
                                                if ($ord['status'] === 'Out for Delivery') $statusClass = 'bg-info text-white';
                                            ?>
                                            <span class="badge badge-status <?= $statusClass ?>"><?= htmlspecialchars($ord['status']) ?></span>
                                        </td>
                                        <td>
                                            <form method="POST" class="d-flex align-items-center gap-1">
                                                <input type="hidden" name="action" value="assign_rider">
                                                <input type="hidden" name="order_id" value="<?= $ord['id'] ?>">
                                                <select name="rider_id" class="form-select form-select-sm" style="min-width: 140px; font-size: 12px;" required>
                                                    <option value="">-- Assign Rider --</option>
                                                    <?php foreach ($riders as $r): ?>
                                                        <option value="<?= $r['id'] ?>" <?= ($ord['assigned_delivery_id'] == $r['id']) ? 'selected' : '' ?>>
                                                            🛵 <?= htmlspecialchars($r['first_name'].' '.$r['last_name']) ?>
                                                        </option>
                                                    <?php endforeach; ?>
                                                </select>
                                                <button type="submit" class="btn btn-sm btn-outline-danger" title="Assign Rider">
                                                    <i class="fa-solid fa-check"></i>
                                                </button>
                                            </form>
                                            <?php if ($ord['assigned_delivery_id']): ?>
                                                <div class="text-success small mt-1"><i class="fa-solid fa-motorcycle me-1"></i><?= htmlspecialchars($ord['rider_first_name'].' '.$ord['rider_last_name']) ?></div>
                                            <?php endif; ?>
                                        </td>
                                        <td>
                                            <div class="dropdown">
                                                <button class="btn btn-light btn-sm border dropdown-toggle" type="button" data-bs-toggle="dropdown">Manage</button>
                                                <ul class="dropdown-menu dropdown-menu-end shadow-sm small">
                                                    <li>
                                                        <form method="POST">
                                                            <input type="hidden" name="action" value="update_order_status">
                                                            <input type="hidden" name="order_id" value="<?= $ord['id'] ?>">
                                                            <input type="hidden" name="status" value="Confirmed">
                                                            <button type="submit" class="dropdown-item text-primary"><i class="fa-solid fa-check me-2"></i>Approve Order</button>
                                                        </form>
                                                    </li>
                                                    <li>
                                                        <form method="POST">
                                                            <input type="hidden" name="action" value="update_order_status">
                                                            <input type="hidden" name="order_id" value="<?= $ord['id'] ?>">
                                                            <input type="hidden" name="status" value="Delivered">
                                                            <button type="submit" class="dropdown-item text-success"><i class="fa-solid fa-box-open me-2"></i>Mark Delivered & Collected</button>
                                                        </form>
                                                    </li>
                                                    <li><hr class="dropdown-divider"></li>
                                                    <li>
                                                        <form method="POST">
                                                            <input type="hidden" name="action" value="update_order_status">
                                                            <input type="hidden" name="order_id" value="<?= $ord['id'] ?>">
                                                            <input type="hidden" name="status" value="Cancelled">
                                                            <button type="submit" class="dropdown-item text-danger"><i class="fa-solid fa-ban me-2"></i>Cancel Order</button>
                                                        </form>
                                                    </li>
                                                </ul>
                                            </div>
                                        </td>
                                    </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Delivered Orders -->
                    <div class="tab-pane fade" id="deliveredOrdersPane">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Order #</th>
                                        <th>Customer</th>
                                        <th>Items</th>
                                        <th>Total Collected</th>
                                        <th>Delivered By</th>
                                        <th>Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <?php if (empty($deliveredOrders)): ?>
                                    <tr>
                                        <td colspan="6" class="text-center py-5 text-muted">
                                            <i class="fa-solid fa-truck-ramp-box fa-3x mb-3 text-secondary d-block"></i>
                                            No delivered orders yet.
                                        </td>
                                    </tr>
                                <?php else: ?>
                                    <?php foreach ($deliveredOrders as $ord): ?>
                                    <tr class="table-success bg-opacity-25">
                                        <td>
                                            <span class="fw-bold text-success"><?= htmlspecialchars($ord['order_number']) ?></span>
                                        </td>
                                        <td>
                                            <div class="fw-semibold"><?= htmlspecialchars($ord['first_name'] ? ($ord['first_name'].' '.$ord['last_name']) : 'Guest') ?></div>
                                            <div class="text-muted small"><?= htmlspecialchars($ord['shipping_address'] ?: '—') ?></div>
                                        </td>
                                        <td>
                                            <?php if (!empty($ord['items'])): ?>
                                                <ul class="list-unstyled mb-0 small">
                                                    <?php foreach ($ord['items'] as $item): ?>
                                                        <li>• <?= htmlspecialchars($item['product_name']) ?> <span class="badge bg-light text-dark">x<?= $item['quantity'] ?></span></li>
                                                    <?php endforeach; ?>
                                                </ul>
                                            <?php endif; ?>
                                        </td>
                                        <td class="fw-bold text-success"><?= htmlspecialchars($ord['total_price']) ?></td>
                                        <td>
                                            <?php if ($ord['rider_first_name']): ?>
                                                <div><i class="fa-solid fa-motorcycle text-success me-1"></i><?= htmlspecialchars($ord['rider_first_name'].' '.$ord['rider_last_name']) ?></div>
                                            <?php else: ?>
                                                <span class="text-muted small">—</span>
                                            <?php endif; ?>
                                        </td>
                                        <td class="small text-muted"><?= date('M d, Y', strtotime($ord['created_at'])) ?></td>
                                    </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Cancelled Orders -->
                    <div class="tab-pane fade" id="cancelledOrdersPane">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                    <tr><th>Order #</th><th>Customer</th><th>Items</th><th>Total</th><th>Date</th></tr>
                                </thead>
                                <tbody>
                                <?php if (empty($cancelledOrders)): ?>
                                    <tr><td colspan="5" class="text-center py-4 text-muted">No cancelled orders.</td></tr>
                                <?php else: ?>
                                    <?php foreach ($cancelledOrders as $ord): ?>
                                    <tr class="text-muted">
                                        <td><span class="fw-bold text-danger"><?= htmlspecialchars($ord['order_number']) ?></span></td>
                                        <td><?= htmlspecialchars($ord['first_name'] ? ($ord['first_name'].' '.$ord['last_name']) : 'Guest') ?></td>
                                        <td>
                                            <?php if (!empty($ord['items'])): ?>
                                                <?php foreach ($ord['items'] as $item): ?>
                                                    <div class="small">• <?= htmlspecialchars($item['product_name']) ?> x<?= $item['quantity'] ?></div>
                                                <?php endforeach; ?>
                                            <?php endif; ?>
                                        </td>
                                        <td><?= htmlspecialchars($ord['total_price']) ?></td>
                                        <td class="small"><?= date('M d, Y', strtotime($ord['created_at'])) ?></td>
                                    </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                                </tbody>
                            </table>
                        </div>
                    </div>

                </div><!-- end tab-content -->
            </div>

            <!-- TAB 3: SERVICES (REFILL & INSPECTION) -->
            <div class="tab-pane fade" id="services-pane">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0">Services: Extinguisher Refilling, Installation & Inspection</h5>
                    <small class="text-muted"><i class="fa-solid fa-wrench text-danger me-1"></i> Sales reviews requests and assigns certified Technicians.</small>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>Request #</th>
                                <th>Service Type</th>
                                <th>Customer & Contact</th>
                                <th>Specifications & Notes</th>
                                <th>Requested Date / Time</th>
                                <th>Assigned Technician</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php if (empty($services)): ?>
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <i class="fa-solid fa-screwdriver-wrench fa-3x mb-3 text-secondary d-block"></i>
                                    No service requests submitted yet. Customers can request refills or inspections from the mobile app!
                                </td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($services as $srv): ?>
                            <tr>
                                <td class="fw-bold text-danger">#SR-<?= $srv['id'] ?></td>
                                <td>
                                    <span class="badge bg-dark rounded-pill px-3 py-2"><?= htmlspecialchars($srv['service_type']) ?></span>
                                </td>
                                <td>
                                    <div class="fw-semibold"><?= htmlspecialchars(($srv['cust_first'].' '.$srv['cust_last']) ?: 'Customer') ?></div>
                                    <div class="small text-muted"><i class="fa-solid fa-phone me-1"></i><?= htmlspecialchars($srv['contact_number'] ?: 'N/A') ?></div>
                                </td>
                                <td class="small text-muted" style="max-width: 200px;">
                                    <?= htmlspecialchars($srv['details'] ?: 'Standard service request') ?>
                                    <div class="mt-1 text-dark"><i class="fa-solid fa-location-dot text-danger me-1"></i><?= htmlspecialchars($srv['address'] ?: 'On-site') ?></div>
                                </td>
                                <td>
                                    <div class="fw-semibold small"><i class="fa-solid fa-calendar-day text-primary me-1"></i><?= htmlspecialchars($srv['service_date'] ?: 'Today') ?></div>
                                    <div class="text-muted small"><?= htmlspecialchars($srv['service_time'] ?: 'Standard Hours') ?></div>
                                </td>
                                <td>
                                    <form method="POST" class="d-flex align-items-center gap-1">
                                        <input type="hidden" name="action" value="assign_technician">
                                        <input type="hidden" name="service_id" value="<?= $srv['id'] ?>">
                                        <select name="tech_id" class="form-select form-select-sm" style="min-width: 140px; font-size: 12px;" required>
                                            <option value="">-- Assign Tech --</option>
                                            <?php foreach ($technicians as $t): ?>
                                                <option value="<?= $t['id'] ?>" <?= ($srv['assigned_technician_id'] == $t['id']) ? 'selected' : '' ?>>
                                                    👨‍🔧 <?= htmlspecialchars($t['first_name'].' '.$t['last_name']) ?>
                                                </option>
                                            <?php endforeach; ?>
                                        </select>
                                        <button type="submit" class="btn btn-sm btn-outline-danger" title="Assign Technician">
                                            <i class="fa-solid fa-check"></i>
                                        </button>
                                    </form>
                                    <?php if ($srv['assigned_technician_id']): ?>
                                        <div class="text-success small mt-1"><i class="fa-solid fa-user-check me-1"></i>Tech: <?= htmlspecialchars($srv['tech_first'].' '.$srv['tech_last']) ?></div>
                                    <?php endif; ?>
                                </td>
                                <td>
                                    <span class="badge badge-status <?= $srv['status'] === 'Completed' ? 'bg-success' : 'bg-warning text-dark' ?>">
                                        <?= htmlspecialchars($srv['status']) ?>
                                    </span>
                                </td>
                                <td>
                                    <form method="POST" class="d-inline">
                                        <input type="hidden" name="action" value="update_service_status">
                                        <input type="hidden" name="service_id" value="<?= $srv['id'] ?>">
                                        <input type="hidden" name="status" value="Completed">
                                        <button type="submit" class="btn btn-sm btn-outline-success" title="Mark Completed">
                                            <i class="fa-solid fa-check-double"></i> Complete
                                        </button>
                                    </form>
                                </td>
                            </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>

            <?php if ($currentUser['role'] === 'admin'): ?>
            <!-- TAB 4: STAFF MANAGEMENT (ADMIN ONLY) -->
            <div class="tab-pane fade" id="staff-pane">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div>
                        <h5 class="fw-bold mb-0">Staff Accounts & Access Control</h5>
                        <small class="text-muted">Admin can create staff or deactivate accounts if a staff member resigns.</small>
                    </div>
                    <button class="btn btn-brand-sm" data-bs-toggle="modal" data-bs-target="#addStaffModal">
                        <i class="fa-solid fa-user-plus me-1"></i> Add New Staff
                    </button>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>Name</th>
                                <th>Username / Email</th>
                                <th>Role</th>
                                <th>Phone Number</th>
                                <th>Account Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($allStaff as $st): ?>
                            <tr>
                                <td class="fw-bold"><?= htmlspecialchars($st['first_name'].' '.$st['last_name']) ?></td>
                                <td>
                                    <div>@<?= htmlspecialchars($st['username']) ?></div>
                                    <div class="text-muted small"><?= htmlspecialchars($st['email']) ?></div>
                                </td>
                                <td>
                                    <?php
                                        $rClass = 'bg-secondary';
                                        if ($st['role'] === 'admin') $rClass = 'bg-danger';
                                        if ($st['role'] === 'sales') $rClass = 'bg-primary';
                                        if ($st['role'] === 'delivery') $rClass = 'bg-warning text-dark';
                                        if ($st['role'] === 'technician') $rClass = 'bg-info text-white';
                                    ?>
                                    <span class="badge <?= $rClass ?> rounded-pill px-3 py-1 text-uppercase" style="font-size: 11px;">
                                        <?= htmlspecialchars($st['role']) ?>
                                    </span>
                                </td>
                                <td><?= htmlspecialchars($st['phone_number'] ?: 'N/A') ?></td>
                                <td>
                                    <?php if ($st['is_active']): ?>
                                        <span class="badge bg-success rounded-pill px-2 py-1"><i class="fa-solid fa-circle-check me-1"></i>Active</span>
                                    <?php else: ?>
                                        <span class="badge bg-danger rounded-pill px-2 py-1"><i class="fa-solid fa-circle-xmark me-1"></i>Deactivated</span>
                                    <?php endif; ?>
                                </td>
                                <td>
                                    <?php if ($st['id'] !== $currentUser['id']): ?>
                                        <div class="d-flex gap-2 align-items-center flex-wrap">
                                            <form method="POST" class="d-inline">
                                                <input type="hidden" name="action" value="toggle_staff">
                                                <input type="hidden" name="staff_id" value="<?= $st['id'] ?>">
                                                <?php if ($st['is_active']): ?>
                                                    <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('Deactivate this staff account? They will not be able to log in.')">
                                                        <i class="fa-solid fa-user-slash me-1"></i> Deactivate
                                                    </button>
                                                <?php else: ?>
                                                    <button type="submit" class="btn btn-sm btn-outline-success">
                                                        <i class="fa-solid fa-user-check me-1"></i> Reactivate
                                                    </button>
                                                <?php endif; ?>
                                            </form>
                                            <form method="POST" class="d-inline">
                                                <input type="hidden" name="action" value="delete_staff">
                                                <input type="hidden" name="staff_id" value="<?= $st['id'] ?>">
                                                <button type="submit" class="btn btn-sm btn-danger"
                                                    onclick="return confirm('⚠️ Permanently DELETE account @<?= htmlspecialchars($st['username']) ?>? This cannot be undone.')">
                                                    <i class="fa-solid fa-trash me-1"></i> Delete
                                                </button>
                                            </form>
                                        </div>
                                    <?php else: ?>
                                        <span class="text-muted small">You (Current)</span>
                                    <?php endif; ?>
                                </td>
                            </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>
            <?php endif; ?>

        </div>
    </div>

</div>

<!-- Modal: Add New Staff (Admin Only) -->
<?php if ($currentUser['role'] === 'admin'): ?>
<div class="modal fade" id="addStaffModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold"><i class="fa-solid fa-user-shield text-danger me-2"></i>Create New Staff Account</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST">
                <div class="modal-body">
                    <input type="hidden" name="action" value="add_staff">
                    
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Full Name</label>
                        <input type="text" name="staff_name" class="form-control rounded-3" placeholder="e.g. John Doe" required>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col">
                            <label class="form-label small fw-semibold">Username</label>
                            <input type="text" name="staff_username" class="form-control rounded-3" placeholder="johndoe" required>
                        </div>
                        <div class="col">
                            <label class="form-label small fw-semibold">Role</label>
                            <select name="staff_role" class="form-select rounded-3" required>
                                <option value="sales">Sales Staff</option>
                                <option value="technician">Technician</option>
                                <option value="delivery">Delivery Rider</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Email</label>
                        <input type="email" name="staff_email" class="form-control rounded-3" placeholder="john@flamepro.com">
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col">
                            <label class="form-label small fw-semibold">Password</label>
                            <input type="password" name="staff_password" class="form-control rounded-3" placeholder="••••••••" required>
                        </div>
                        <div class="col">
                            <label class="form-label small fw-semibold">Phone Number</label>
                            <input type="text" name="staff_phone" class="form-control rounded-3" placeholder="09123456789">
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-danger rounded-pill px-4 fw-semibold" style="background: #E50914;">Create Account</button>
                </div>
            </form>
        </div>
    </div>
</div>
<?php endif; ?>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<!-- Initialize Charts -->
<script>
document.addEventListener("DOMContentLoaded", function () {
    // 1. Category Pie Chart
    const pieCtx = document.getElementById('categoryPieChart').getContext('2d');
    const pieLabels = <?= json_encode($chartLabels) ?>;
    const pieData = <?= json_encode($chartValues) ?>;
    const pieColors = <?= json_encode(array_slice($chartColors, 0, count($chartLabels))) ?>;

    new Chart(pieCtx, {
        type: 'doughnut',
        data: {
            labels: pieLabels,
            datasets: [{
                data: pieData,
                backgroundColor: pieColors,
                borderWidth: 2,
                borderColor: '#FFFFFF',
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        boxWidth: 12,
                        padding: 14,
                        font: { family: 'Poppins', size: 11, weight: '500' }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let val = context.raw || 0;
                            return ' ' + context.label + ': ₱ ' + Number(val).toLocaleString(undefined, {minimumFractionDigits: 2});
                        }
                    }
                }
            },
            cutout: '60%'
        }
    });

    // 2. Monthly Trend Bar Chart
    const barCtx = document.getElementById('monthlyBarChart').getContext('2d');
    const monthLabels = <?= json_encode($monthLabels) ?>;
    const monthTotals = <?= json_encode($monthlyChartData) ?>;

    new Chart(barCtx, {
        type: 'bar',
        data: {
            labels: monthLabels,
            datasets: [{
                label: 'Monthly Revenue (₱)',
                data: monthTotals,
                backgroundColor: 'rgba(229, 9, 20, 0.85)',
                borderColor: '#E50914',
                borderWidth: 1.5,
                borderRadius: 8,
                maxBarThickness: 32
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) { return '₱' + value; },
                        font: { family: 'Poppins', size: 11 }
                    },
                    grid: { color: 'rgba(0,0,0,0.05)' }
                },
                x: {
                    grid: { display: false },
                    ticks: { font: { family: 'Poppins', size: 11 } }
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return ' Revenue: ₱ ' + Number(context.raw).toLocaleString(undefined, {minimumFractionDigits: 2});
                        }
                    }
                }
            }
        }
    });
});
</script>

</body>
</html>
