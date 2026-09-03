<?php
// get_tdls.php - Fetch all TDLs with their images
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'includes/db.php';

try {
    // Fetch all active TDLs
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
        WHERE t.is_active = 1
        ORDER BY t.sort_order ASC, t.created_at DESC
    ");
    $stmt->execute();
    $tdls = $stmt->fetchAll();

    // Fetch images for each TDL
    $tdlIds = array_column($tdls, 'id');
    $images = [];
    
    if (!empty($tdlIds)) {
        $placeholders = implode(',', array_fill(0, count($tdlIds), '?'));
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
            WHERE tdl_id IN ($placeholders)
            ORDER BY is_primary DESC, sort_order ASC
        ");
        $imgStmt->execute($tdlIds);
        $allImages = $imgStmt->fetchAll();
        
        foreach ($allImages as $img) {
            $images[$img['tdl_id']][] = $img;
        }
    }

    // Combine TDLs with their images
    $result = [];
    foreach ($tdls as $tdl) {
        $tdl['images'] = $images[$tdl['id']] ?? [];
        // Add primary image for easy access
        $tdl['primary_image'] = null;
        foreach ($tdl['images'] as $img) {
            if ($img['is_primary'] == 1) {
                $tdl['primary_image'] = $img['image_url'];
                break;
            }
        }
        // If no primary image, use first image
        if (!$tdl['primary_image'] && !empty($tdl['images'])) {
            $tdl['primary_image'] = $tdl['images'][0]['image_url'];
        }
        $result[] = $tdl;
    }

    echo json_encode([
        'status' => 'success',
        'message' => 'TDLs fetched successfully',
        'count' => count($result),
        'data' => $result
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Database error: ' . $e->getMessage(),
        'data' => []
    ]);
} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        'status' => 'error',
        'message' => $e->getMessage(),
        'data' => []
    ]);
}
?>
