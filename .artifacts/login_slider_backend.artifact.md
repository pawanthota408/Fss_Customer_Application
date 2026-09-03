# Login Slider Backend Implementation

## 1. SQL: Create Tables
Run this SQL in your database to create the required tables.

```sql
-- Slider images
CREATE TABLE IF NOT EXISTS `login_sliders` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `image_url` VARCHAR(255) NOT NULL,
  `title` VARCHAR(100) DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `status` ENUM('Active', 'Inactive') DEFAULT 'Active',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- App Branding (Logo etc)
CREATE TABLE IF NOT EXISTS `app_settings` (
  `id` INT PRIMARY KEY DEFAULT 1,
  `logo_url` VARCHAR(255) NOT NULL,
  `app_name` VARCHAR(100) DEFAULT 'Friends Software Solutions'
);

-- Sample Data
INSERT INTO `app_settings` (`logo_url`) VALUES ('https://crm.friendssoftwaresolutions.in/api/customers/logo/hand.png');

INSERT INTO `login_sliders` (`image_url`, `title`, `description`) VALUES
('https://crm.friendssoftwaresolutions.in/api/customers/sliders/slider1.png', 'Dedicated Support', '24 Hours Dedicated Support for your business'),
('https://crm.friendssoftwaresolutions.in/api/customers/sliders/slider2.png', 'Tally Solutions', 'Comprehensive Tally Solutions for Growth'),
('https://crm.friendssoftwaresolutions.in/api/customers/sliders/slider3.png', 'Expert Development', 'Expert Website & App Development Services');
```

## 2. PHP: `login_sliders.php`
Create this file in your `api/customers/` folder.

```php
<?php
// login_sliders.php
require_once 'config.php';
header('Content-Type: application/json');

try {
    // 1. Fetch Sliders
    $stmt = $conn->prepare("SELECT id, image_url, title, description FROM login_sliders WHERE status = 'Active' ORDER BY id ASC");
    $stmt->execute();
    $sliders = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // 2. Fetch Logo
    $stmt = $conn->prepare("SELECT logo_url FROM app_settings LIMIT 1");
    $stmt->execute();
    $settings = $stmt->fetch(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => "success",
        "sliders" => $sliders,
        "logo_url" => $settings['logo_url'] ?? ""
    ]);

} catch(PDOException $e) {
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
?>
```
