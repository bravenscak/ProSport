package hr.java.web.prosport.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadServiceImpl implements ImageUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        log.info("Starting image upload for file: {}", file.getOriginalFilename());

        validateFile(file);
        createUploadDirectoryIfNotExists();

        String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
        Path filePath = Paths.get(uploadDir).resolve(uniqueFilename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String imageUrl = "/uploads/" + uniqueFilename;
        log.info("Image uploaded successfully: {}", imageUrl);

        return imageUrl;
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
            log.warn("Invalid image URL for deletion: {}", imageUrl);
            return;
        }

        try {
            String filename = imageUrl.substring("/uploads/".length());
            Path filePath = Paths.get(uploadDir, filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Image deleted successfully: {}", imageUrl);
            } else {
                log.warn("Image file not found for deletion: {}", imageUrl);
            }
        } catch (IOException e) {
            log.error("Failed to delete image: {}", imageUrl, e);
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("File is null or empty");
            return false;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size too large: {} bytes (max: {} bytes)", file.getSize(), MAX_FILE_SIZE);
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Invalid content type: {}", contentType);
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                log.warn("Invalid file extension: {}", extension);
                return false;
            }
        } else {
            log.warn("Original filename is null");
            return false;
        }

        return true;
    }

    @Override
    public String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());

        return "img_" + timestamp + "_" + uuid + extension;
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Datoteka je prazna ili nije odabrana");
        }

        if (!isValidImage(file)) {
            throw new IllegalArgumentException("Datoteka mora biti validna slika (JPG, PNG, GIF, WebP) i manja od 10MB");
        }
    }

    private void createUploadDirectoryIfNotExists() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}