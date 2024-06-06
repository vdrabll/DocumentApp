package ru.demo.documentsapp.services;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MinioStorageService {

    void removeFile(String filename);

    InputStreamResource getObject(String filename);

    String getExtension(MultipartFile file);

    void saveFile(MultipartFile file, String extension, UUID uuid);
}
