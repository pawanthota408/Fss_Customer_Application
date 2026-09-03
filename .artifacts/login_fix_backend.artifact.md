# Login Backend Fix & Configuration

The "database error" during login usually happens due to a missing column, a typo in the table name, or a connection issue in `config.php`.

## 1. Updated `login.php`
Replace your `login.php` with this version. It has improved error logging to help you see exactly what's failing.

```php
<?php
// login.php

// 1. Prevent any accidental output before JSON
ob_start();
error_reporting(E_ALL);
ini_set('display_errors', 0); // Don't show raw errors to user

require_once 'config.php';

// 2. Set JSON header
ob_clean();
header('Content-Type: application/json; charset=utf-8');

// 3. Get JSON input from Android
$input = file_get_contents("php://input");
$data = json_decode($input, true);

if (!$data) {
    echo json_encode(["status" => "error", "message" => "No data received"]);
    exit;
}

$email = trim($data['email'] ?? '');
$password = trim($data['password'] ?? '');

if (empty($email) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "Credentials are required"]);
    exit;
}

try {
    // 4. Query the database
    // Note: We check against both email and phone column in customer_users
    $stmt = $conn->prepare("SELECT id, email, password FROM customer_users WHERE email = :email OR phone = :email LIMIT 1");
    $stmt->execute([':email' => $email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user) {
        // 5. Verify Password
        // IMPORTANT: If you use plain text passwords, use $password == $user['password']
        // If you use hashing (Recommended), use password_verify($password, $user['password'])
        if ($password == $user['password']) {
            echo json_encode([
                "status" => "success",
                "message" => "Login Successful",
                "userId" => (int)$user['id']
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Invalid password"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "User account not found"]);
    }

} catch (PDOException $e) {
    // 6. Detailed Error for Debugging
    echo json_encode([
        "status" => "error",
        "message" => "Database Error: " . $e->getMessage()
    ]);
}
?>
```

## 2. Check your `config.php`
Ensure your `config.php` uses PDO and has the correct credentials.

```php
<?php
// config.php
$host = 'localhost';
$db   = 'YOUR_DATABASE_NAME';
$user = 'YOUR_USERNAME';
$pass = 'YOUR_PASSWORD';
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
     $conn = new PDO($dsn, $user, $pass, $options);
} catch (\PDOException $e) {
     // This will show if the connection itself is failing
     die(json_encode(["status" => "error", "message" => "Connection Failed: " . $e->getMessage()]));
}
?>
```

## 3. Potential SQL Fix
If the error says **"Unknown column 'phone'"**, run this SQL in your phpMyAdmin:

```sql
ALTER TABLE `customer_users` ADD `phone` VARCHAR(20) NULL AFTER `email`;
```
