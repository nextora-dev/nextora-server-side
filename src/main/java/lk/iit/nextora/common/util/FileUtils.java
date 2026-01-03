package lk.iit.nextora.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for file operations.
 * All methods are static - no instantiation needed.
 *
 * Usage:
 * <pre>
 * String ext = FileUtils.getExtension("document.pdf");
 * boolean isImage = FileUtils.isImageFile(multipartFile);
 * String uniqueName = FileUtils.generateUniqueFileName("photo.jpg");
 * </pre>
 */
public final class FileUtils {

    private FileUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    // Allowed file types
    public static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "svg");
    public static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
    public static final List<String> VIDEO_EXTENSIONS = Arrays.asList("mp4", "avi", "mov", "wmv", "mkv");
    public static final List<String> AUDIO_EXTENSIONS = Arrays.asList("mp3", "wav", "ogg", "flac", "aac");

    // Content types
    public static final List<String> IMAGE_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );
    public static final List<String> DOCUMENT_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    // Size limits
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

    // ==================== Extension Methods ====================

    public static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) return "";
        return filename.substring(lastDot + 1).toLowerCase();
    }

    public static String getExtension(MultipartFile file) {
        return getExtension(file.getOriginalFilename());
    }

    public static String getNameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) return filename;
        return filename.substring(0, lastDot);
    }

    // ==================== File Type Validation ====================

    public static boolean isImageFile(String filename) {
        return IMAGE_EXTENSIONS.contains(getExtension(filename));
    }

    public static boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && IMAGE_CONTENT_TYPES.contains(contentType);
    }

    public static boolean isDocumentFile(String filename) {
        return DOCUMENT_EXTENSIONS.contains(getExtension(filename));
    }

    public static boolean isDocumentFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && DOCUMENT_CONTENT_TYPES.contains(contentType);
    }

    public static boolean isVideoFile(String filename) {
        return VIDEO_EXTENSIONS.contains(getExtension(filename));
    }

    public static boolean isAudioFile(String filename) {
        return AUDIO_EXTENSIONS.contains(getExtension(filename));
    }

    public static boolean hasValidExtension(String filename, List<String> allowedExtensions) {
        return allowedExtensions.contains(getExtension(filename));
    }

    public static boolean hasValidExtension(MultipartFile file, List<String> allowedExtensions) {
        return hasValidExtension(file.getOriginalFilename(), allowedExtensions);
    }

    // ==================== Size Validation ====================

    public static boolean isFileSizeValid(MultipartFile file, long maxSize) {
        return file.getSize() <= maxSize;
    }

    public static boolean isFileSizeValid(MultipartFile file) {
        return isFileSizeValid(file, MAX_FILE_SIZE);
    }

    public static boolean isImageSizeValid(MultipartFile file) {
        return isFileSizeValid(file, MAX_IMAGE_SIZE);
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // ==================== Unique Name Generation ====================

    public static String generateUniqueFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    public static String generateUniqueFileName(String prefix, String originalFilename) {
        String extension = getExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "_" + timestamp + "_" + uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    public static String generateDateBasedPath(String baseDir) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return baseDir + "/" + now.getYear() + "/" +
               String.format("%02d", now.getMonthValue()) + "/" +
               String.format("%02d", now.getDayOfMonth());
    }

    // ==================== Content Type ====================

    public static String getContentType(String filename) {
        String extension = getExtension(filename);
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }

    // ==================== File Operations ====================

    public static boolean isEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    public static boolean isNotEmpty(MultipartFile file) {
        return !isEmpty(file);
    }

    public static void createDirectoryIfNotExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    public static void deleteIfExists(Path file) throws IOException {
        Files.deleteIfExists(file);
    }

    // ==================== Sanitization ====================

    public static String sanitizeFileName(String filename) {
        if (filename == null) return "unnamed";
        // Remove path separators and dangerous characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_")
                      .replaceAll("^_|_$", "");
    }

    public static boolean isSafeFileName(String filename) {
        if (filename == null || filename.isEmpty()) return false;
        // Check for path traversal attempts
        return !filename.contains("..") &&
               !filename.contains("/") &&
               !filename.contains("\\") &&
               !filename.startsWith(".");
    }
}
