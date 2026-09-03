# Dashboard Backend Implementation

To support the new Dashboard UI, you need to update your MySQL database and create the `dashboard.php` API.

## 1. Updated SQL Logic

### Table Join Strategy
To map `customer_users` to `customers`, use a `JOIN` on the email or a shared ID. Assuming `customer_users.id` corresponds to a foreign key in `customers` (or matching email).

```sql
-- Fetching Dashboard Data for a specific user
SELECT
    c.id, c.name, c.company, c.licences, c.closing_date,
    u.id as user_id
FROM customers c
JOIN customer_users u ON c.email = u.email
WHERE u.id = :user_id
LIMIT 1;
```

### Products and Services Fetching
```sql
-- Fetch Products
SELECT * FROM products_services WHERE category = 'Product' AND status = 'Active';

-- Fetch Services
SELECT * FROM products_services WHERE category = 'Service' AND status = 'Active';
```

## 2. Updated `dashboard.php`
This script returns all the data required for the Dashboard in a single JSON response.

```php
<?php
// dashboard.php
require_once 'config.php';
header('Content-Type: application/json');

$userId = $_GET['user_id'];

if (!empty($userId)) {
    try {
        // 1. Fetch Customer Details
        $query = "SELECT id, name, company, licences, closing_date FROM customers WHERE email = (SELECT email FROM customer_users WHERE id = :user_id) LIMIT 1";
        $stmt = $conn->prepare($query);
        $stmt->execute([':user_id' => $userId]);
        $customer = $stmt->fetch(PDO::FETCH_ASSOC);

        // 2. Fetch Products
        $stmt = $conn->prepare("SELECT id, name, category, icon_link FROM products_services WHERE category = 'Product'");
        $stmt->execute();
        $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // 3. Fetch Services
        $stmt = $conn->prepare("SELECT id, name, category, icon_link FROM products_services WHERE category = 'Service'");
        $stmt->execute();
        $services = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // 4. Extract Licences into a List (Assuming comma-separated string in DB)
        $licence_list = [];
        if (!empty($customer['licences'])) {
            $nums = explode(',', $customer['licences']);
            foreach ($nums as $num) {
                $licence_list[] = [
                    "number" => trim($num),
                    "status" => "Active",
                    "validTill" => $customer['closing_date']
                ];
            }
        }

        echo json_encode([
            "status" => "success",
            "customer" => $customer,
            "products" => $products,
            "services" => $services,
            "licence_list" => $licence_list
        ]);

    } catch(PDOException $e) {
        echo json_encode(["status" => "error", "message" => $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "User ID missing"]);
}
?>
```
