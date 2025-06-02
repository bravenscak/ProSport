package hr.java.web.prosport.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageUploadService {
    String uploadImage(MultipartFile file) throws IOException;
    void deleteImage(String imageUrl);
    boolean isValidImage(MultipartFile file);
    String generateUniqueFilename(String originalFilename);
}