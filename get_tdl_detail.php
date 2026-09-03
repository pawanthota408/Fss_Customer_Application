<?php
// get_tdl_detail.php - Fetch single TDL by ID with all images
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'includes/db.php';

try {
    $tdlId = isset($_GET['id']) ? intval($_GET['id']) : 0;

    if ($tdlId <= 0) {
        throw new Exception('Invalid TDL ID');
    }

    // Fetch TDL details
    $stmt = $pdo->prepare("
        SELECT 
            t.id,
            t.name,
            t.slug,
            t.short_desc,
            t.full_desc,
            t.category,
            t.price,
            t.is_featured,
            t.is_active,
            t.download_link,
            t.demo_video,
            t.compatibility,
            t.sort_order,
            t.meta_title,
            t.meta_description,
            t.created_at,
            t.updated_at
        FROM tdls t
        WHERE t.id = ? AND t.is_active = 1
    ");
    $stmt->execute([$tdlId]);
    $tdl = $stmt->fetch();

    if (!$tdl) {
        throw new Exception('TDL not found');
    }

    // Fetch images
    $imgStmt = $pdo->prepare("
        SELECT 
            id,
            tdl_id,
            image_url,
            alt_text,
            sort_order,
            is_primary,
            created_at
        FROM tdl_images
        WHERE tdl_id = ?
        ORDER BY is_primary DESC, sort_order ASC
    ");
    $imgStmt->execute([$tdlId]);
    $images = $imgStmt->fetchAll();

    $tdl['images'] = $images;
    $tdl['primary_image'] = null;
    foreach ($images as $img) {
        if ($img['is_primary'] == 1) {
            $tdl['primary_image'] = $img['image_url'];
            break;
        }
    }
    if (!$tdl['primary_image'] && !empty($images)) {
        $tdl['primary_image'] = $images[0]['image_url'];
    }

    echo json_encode([
        'status' => 'success',
        'message' => 'TDL details fetched successfully',
        'data' => $tdl
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Database error: ' . $e->getMessage(),
        'data' => null
    ]);
} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        'status' => 'error',
        'message' => $e->getMessage(),
        'data' => null
    ]);
}
?>
