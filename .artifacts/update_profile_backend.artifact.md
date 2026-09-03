# Profile Update Backend Implementation

## 1. PHP: `update_profile.php`
Create this file in your `api/customers/` folder. This script handles updates to personal information, company details, and password security.

```php
<?php
// update_profile.php
require_once 'config.php';
header('Content-Type: application/json');

// Get POST data
$data = json_decode(file_get_contents("php://input"), true);
$userId = $data['user_id'] ?? null;
$type = $data['update_type'] ?? ''; // 'personal', 'company', or 'password'

if (!$userId) {
    echo json_encode(["status" => "error", "message" => "User ID is required"]);
    exit;
}

try {
    if ($type === 'personal') {
        $name = $data['name'] ?? '';
        $email = $data['email'] ?? '';
        $phone = $data['phone'] ?? '';

        $stmt = $conn->prepare("UPDATE customer_users SET name = :name, email = :email, phone = :phone WHERE id = :id");
        $stmt->execute([':name' => $name, ':email' => $email, ':phone' => $phone, ':id' => $userId]);

        echo json_encode(["status" => "success", "message" => "Personal info updated successfully"]);

    } elseif ($type === 'company') {
        $company = $data['company_name'] ?? '';
        $address = $data['address'] ?? '';

        // Update company details in the 'customers' table linked to this user
        $stmt = $conn->prepare("UPDATE customers SET company = :company, address = :address WHERE id = (SELECT customer_id FROM customer_users WHERE id = :id)");
        $stmt->execute([':company' => $company, ':address' => $address, ':id' => $userId]);

        echo json_encode(["status" => "success", "message" => "Company details updated successfully"]);

    } elseif ($type === 'password') {
        $newPass = $data['new_password'] ?? '';

        if (empty($newPass)) {
            echo json_encode(["status" => "error", "message" => "New password cannot be empty"]);
            exit;
        }

        $stmt = $conn->prepare("UPDATE customer_users SET password = :pass WHERE id = :id");
        $stmt->execute([':pass' => $newPass, ':id' => $userId]);

        echo json_encode(["status" => "success", "message" => "Password changed successfully"]);

    } else {
        echo json_encode(["status" => "error", "message" => "Invalid update type"]);
    }

} catch(PDOException $e) {
    echo json_encode(["status" => "error", "message" => "Database Error: " . $e->getMessage()]);
}
?>
```
