package hr.java.web.prosport.service;

import hr.java.web.prosport.exception.ImageUploadException;
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

    private static final String UPLOADS_PREFIX = "/uploads/";
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final String EMPTY_FILE_MSG = "Datoteka je prazna ili nije odabrana";
    private static final String INVALID_IMAGE_MSG = "Datoteka mora biti validna slika (JPG, PNG, GIF, WebP) i manja od 10MB";
    private static final String FILE_SIZE_WARNING = "File size too large: {} bytes (max: {} bytes)";
    private static final String INVALID_CONTENT_TYPE_WARNING = "Invalid content type: {}";
    private static final String INVALID_EXTENSION_WARNING = "Invalid file extension: {}";
    private static final String NULL_FILENAME_WARNING = "Original filename is null";
    private static final String FILE_NULL_OR_EMPTY_WARNING = "File is null or empty";
    private static final String UPLOAD_DIR_CREATED_INFO = "Created upload directory: {}";
    private static final String IMAGE_UPLOADED_SUCCESS = "Image uploaded successfully: {}";
    private static final String IMAGE_DELETED_SUCCESS = "Image deleted successfully: {}";
    private static final String IMAGE_NOT_FOUND_WARNING = "Image file not found for deletion: {}";
    private static final String DELETE_FAILED_ERROR = "Failed to delete image: {}";
    private static final String INVALID_URL_WARNING = "Invalid image URL for deletion: {}";

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String uploadImage(MultipartFile file) {
        log.info("Starting image upload for file: {}", file.getOriginalFilename());

        validateFile(file);
        createUploadDirectoryIfNotExists();

        String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
        Path filePath = Paths.get(uploadDir).resolve(uniqueFilename);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image: " + e.getMessage(), e);
        }

        String imageUrl = UPLOADS_PREFIX + uniqueFilename;
        log.info(IMAGE_UPLOADED_SUCCESS, imageUrl);

        return imageUrl;
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(UPLOADS_PREFIX)) {
            log.warn(INVALID_URL_WARNING, imageUrl);
            return;
        }

        try {
            String filename = imageUrl.substring(UPLOADS_PREFIX.length());
            Path filePath = Paths.get(uploadDir, filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info(IMAGE_DELETED_SUCCESS, imageUrl);
            } else {
                log.warn(IMAGE_NOT_FOUND_WARNING, imageUrl);
            }
        } catch (IOException e) {
            log.error(DELETE_FAILED_ERROR, imageUrl, e);
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn(FILE_NULL_OR_EMPTY_WARNING);
            return false;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn(FILE_SIZE_WARNING, file.getSize(), MAX_FILE_SIZE);
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn(INVALID_CONTENT_TYPE_WARNING, contentType);
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                log.warn(INVALID_EXTENSION_WARNING, extension);
                return false;
            }
        } else {
            log.warn(NULL_FILENAME_WARNING);
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

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException(EMPTY_FILE_MSG);
        }

        if (!isValidImage(file)) {
            throw new ImageUploadException(INVALID_IMAGE_MSG);
        }
    }

    private void createUploadDirectoryIfNotExists() {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info(UPLOAD_DIR_CREATED_INFO, uploadPath.toAbsolutePath());
            } catch (IOException e) {
                throw new ImageUploadException("Failed to create upload directory: " + e.getMessage(), e);
            }
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}