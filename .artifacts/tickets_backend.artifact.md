# Tickets & Requests Backend Implementation

## 1. SQL: Create `support_requests` Table
Run this in your database to store customer requests. This table acts as the source for the "My Tickets" page.

```sql
CREATE TABLE `support_requests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `category` enum('Product','Service') DEFAULT 'Service',
  `description` text DEFAULT NULL,
  `status` varchar(50) DEFAULT 'Pending', -- 'Pending', 'In Progress', 'Completed', 'Rejected'
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 2. PHP: `submit_ticket.php`
Create this in your `api/customers/` folder. This script receives the inquiry from the Android app and saves it as a new ticket.

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
$category = $data['category'] ?? 'Service';
$desc = $data['description'] ?? '';

try {
    // 1. Get the Customer ID linked to this user
    $stmt = $conn->prepare("SELECT customer_id FROM customer_users WHERE id = :id");
    $stmt->execute([':id' => $userId]);
    $cid = $stmt->fetchColumn();

    if (!$cid) {
        echo json_encode(["status" => "error", "message" => "Customer record not found"]);
        exit;
    }

    // 2. Insert into support_requests (Lead)
    $stmt = $conn->prepare("
        INSERT INTO support_requests (user_id, customer_id, subject, category, description, status)
        VALUES (:uid, :cid, :sub, :cat, :desc, 'Pending')
    ");

    $stmt->execute([
        ':uid' => $userId,
        ':cid' => $cid,
        ':sub' => $subject,
        ':cat' => $category,
        ':desc' => $desc
    ]);

    echo json_encode([
        "status" => "success",
        "message" => "Your request has been raised successfully! Our team will contact you soon."
    ]);

} catch(PDOException $e) {
    echo json_encode(["status" => "error", "message" => "Database Error: " . $e->getMessage()]);
}
?>
```

## 3. Updated `dashboard.php` (Tickets List)
Make sure your `dashboard.php` also includes the logic to fetch the latest tickets. Add this section to the `try` block:

```php
    // FETCH TICKETS
    $stmt = $conn->prepare("
        SELECT id, subject, description, status, category, created_at, updated_at
        FROM support_requests
        WHERE customer_id = :cid
        ORDER BY created_at DESC
        LIMIT 20
    ");
    $stmt->execute([':cid' => $cid]);
    $tickets = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Map for Android
    $ticketList = [];
    foreach($tickets as $t) {
        $ticketList[] = [
            "id" => (int)$t['id'],
            "subject" => $t['subject'],
            "description" => $t['description'],
            "status" => $t['status'],
            "category" => $t['category'],
            "created_at" => date("d M Y, h:i A", strtotime($t['created_at'])),
            "updated_at" => $t['updated_at'] ? date("d M Y, h:i A", strtotime($t['updated_at'])) : null
        ];
    }

    // Include in JSON output
    echo json_encode([
        "status" => "success",
        "customer" => $customerData,
        "products" => $products,
        "services" => $services,
        "licence_list" => $licenceList,
        "tickets" => $ticketList // Add this line
    ]);
```
