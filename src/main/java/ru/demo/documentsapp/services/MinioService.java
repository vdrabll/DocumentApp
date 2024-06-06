package ru.demo.documentsapp.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService implements MinioStorageService {
    @Value("${minio.bucket}")
    private String bucket;
    private final MinioClient minioClient;

    public void putObject(String name, MultipartFile file) {
        try {
            var stream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(name)
                    .stream(stream, stream.available(), -1)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void removeFile(String filename) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public InputStreamResource getObject(String filename) {
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(bucket)
                    .object(filename)
                    .build());
            return new InputStreamResource(stream);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public String getExtension(MultipartFile file) {
        try {
            return Objects
                    .requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Bad extension");
        }
    }

    @Override
    public void saveFile(MultipartFile file, String extension, UUID uuid) {
        String name = uuid.toString() + "." + extension;
        putObject(name, file);
    }
}
