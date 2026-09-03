# Updated Lead Submission Backend

This script updates `submit_ticket.php` to insert requests into the `leads` table and automatically assign them to the correct employee.

## PHP: `submit_ticket.php` (Version 2)
Replace your `submit_ticket.php` with this code.

```php
<?php
// submit_ticket.php
require_once 'config.php';
header('Content-Type: application/json; charset=utf-8');

$input = file_get_contents("php://input");
$data = json_decode($input, true);

if (!$data) {
    echo json_encode(["status" => "error", "message" => "No data received"]);
    exit;
}

$userId = $data['user_id'] ?? 0;
$subject = $data['subject'] ?? 'New Request';
$category = $data['category'] ?? 'Service'; // 'Product' or 'Service'
$desc = $data['description'] ?? '';
$mobile = $data['mobile'] ?? '';
$email = $data['email'] ?? '';
$company = $data['company'] ?? '';
$name = $data['name'] ?? ''; // Added field for name
$licenseNo = $data['license_no'] ?? null;
$ipAddress = $_SERVER['REMOTE_ADDR'] ?? '';

try {
    // 1. Get the Customer details and assigned Employee (emp_id)
    $stmt = $conn->prepare("
        SELECT
            u.customer_id,
            c.name as cust_name,
            c.emp_id
        FROM customer_users u
        LEFT JOIN customers c ON u.customer_id = c.id
        WHERE u.id = :id
    ");
    $stmt->execute([':id' => $userId]);
    $customerInfo = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$customerInfo || !$customerInfo['customer_id']) {
        echo json_encode(["status" => "error", "message" => "Customer record not found"]);
        exit;
    }

    $cid = $customerInfo['customer_id'];
    $assignedTo = $customerInfo['emp_id']; // This is the employee who "won" or owns the customer

    // Use the name from form if provided, otherwise use name from customers table
    $finalName = !empty($name) ? $name : $customerInfo['cust_name'];

    // 2. Insert into leads table
    $query = "INSERT INTO leads (
                customer_id, license_key, name, email, phone,
                company, message, status, source, lead_type,
                service, ip_address, assigned_to, created_by, created_at
              ) VALUES (
                :cid, :lkey, :name, :email, :phone,
                :company, :msg, 'Pending', 'App', :ltype,
                :service, :ip, :assigned, :created_by, NOW()
              )";

    $stmtLeads = $conn->prepare($query);
    $stmtLeads->execute([
        ':cid'        => $cid,
        ':lkey'       => $licenseNo,
        ':name'       => $finalName,
        ':email'      => $email,
        ':phone'      => $mobile,
        ':company'    => $company,
        ':msg'        => $desc,
        ':ltype'      => $category,
        ':service'    => $subject, // Storing subject as requested service
        ':ip'         => $ipAddress,
        ':assigned'   => $assignedTo,
        ':created_by' => $userId
    ]);

    // 3. Also insert into support_requests so the customer can track it in "My Tickets"
    $stmtReq = $conn->prepare("
        INSERT INTO support_requests (user_id, customer_id, subject, category, description, status)
        VALUES (:uid, :cid, :sub, :cat, :desc, 'Pending')
    ");

    $stmtReq->execute([
        ':uid'  => $userId,
        ':cid'  => $cid,
        ':sub'  => $subject,
        ':cat'  => $category,
        ':desc' => $desc
    ]);

    echo json_encode([
        "status" => "success",
        "message" => "Request submitted successfully! Assigned to employee ID: $assignedTo"
    ]);

} catch(PDOException $e) {
    echo json_encode(["status" => "error", "message" => "Database Error: " . $e->getMessage()]);
}
?>
```
