# Renewal Details Backend Implementation

## 1. PHP: `renewal_details.php`
Create this file in your `api/customers/` folder. This script fetches the current license status and calculates the pricing dynamically.

```php
<?php
// renewal_details.php
require_once 'config.php';
header('Content-Type: application/json');

$licenseNo = $_GET['license_number'];

if (empty($licenseNo)) {
    echo json_encode(["status" => "error", "message" => "License number required"]);
    exit;
}

try {
    // 1. Fetch License and Product Details
    $stmt = $conn->prepare("
        SELECT
            cl.license_key,
            cl.expiry_date,
            ps.name as product_name,
            ps.icon_link,
            cl.license_type
        FROM customer_licenses cl
        JOIN products_services ps ON cl.product_id = ps.id
        WHERE cl.license_key = :license_no
        LIMIT 1
    ");
    $stmt->execute([':license_no' => $licenseNo]);
    $license = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$license) {
        echo json_encode(["status" => "error", "message" => "License not found"]);
        exit;
    }

    // 2. Calculate Expiry Logic
    $today = new DateTime();
    $expiry = new DateTime($license['expiry_date']);
    $daysLeft = $today->diff($expiry)->format("%r%a");

    // Calculate Next Expiry (e.g., 1 Year from current expiry)
    $nextExpiry = clone $expiry;
    $nextExpiry->modify('+1 year');

    // 3. Pricing Logic (Sample - You can link this to a 'prices' table)
    $subtotal = 18000.00; // Hardcoded sample, should be from your products table
    $gst_rate = 0.18;
    $gst = $subtotal * $gst_rate;
    $total = $subtotal + $gst;

    // 4. Construct Response
    $details = [
        "license_number" => $license['license_key'],
        "product_name" => $license['product_name'],
        "current_expiry" => date("d M Y", strtotime($license['expiry_date'])),
        "next_expiry" => $nextExpiry->format("d M Y"),
        "plan_type" => ($license['license_type'] ?? "Silver Plan") . " | 1 Year",
        "subtotal" => (double)$subtotal,
        "gst" => (double)$gst,
        "total" => (double)$total,
        "icon_link" => $license['icon_link'],
        "days_left" => (int)$daysLeft
    ];

    echo json_encode([
        "status" => "success",
        "data" => $details
    ]);

} catch(PDOException $e) {
    echo json_encode(["status" => "error", "message" => "DB Error: " . $e->getMessage()]);
}
?>
```
