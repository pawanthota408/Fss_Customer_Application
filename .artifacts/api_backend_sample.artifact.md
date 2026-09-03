# PHP Backend Sample for MySQL Connection

To connect your Android app to a MySQL database, you need a server-side script (PHP) that acts as an intermediary.

## 1. `config.php`
This file contains the database connection settings.

```php
<?php
// config.php
$host = "localhost";
$db_name = "your_database_name";
$username = "your_username";
$password = "your_password";

try {
    $conn = new PDO("mysql:host=$host;dbname=$db_name", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    echo "Connection failed: " . $e->getMessage();
    exit;
}
?>
```

## 2. `login.php`
This file handles the login request from the Android app.

```php
<?php
// login.php
require_once 'config.php';

// Get the posted JSON data
$data = json_decode(file_get_contents("php://input"));

if (!empty($data->email) && !empty($data->password)) {
    $email = $data->email;
    $password = $data->password; // In real apps, use password_verify()

    $query = "SELECT id, name FROM users WHERE email = :email AND password = :password LIMIT 1";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(':email', $email);
    $stmt->bindParam(':password', $password);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        echo json_encode([
            "status" => "success",
            "message" => "Login successful",
            "user_id" => $user['id'],
            "name" => $user['name']
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Invalid email or password"
        ]);
    }
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Incomplete data"
    ]);
}
?>
```
