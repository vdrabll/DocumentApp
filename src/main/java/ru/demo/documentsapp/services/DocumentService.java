package ru.demo.documentsapp.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.demo.documentsapp.entities.Document;
import ru.demo.documentsapp.exceptions.NotFoundException;
import ru.demo.documentsapp.repositories.DocumentRepository;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    @Value("${minio.bucket}")
    private String bucket;

    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;

    @Transactional(readOnly = true)
    public Page<Document> findAll(String name, String extension, Long fileSize, String mimeType, Pageable pageable) {
        Page<Document> all = documentRepository.findAll(pageable);
        List<Document> list = all.stream()
                .filter(document -> {
                    if (name != null) {
                        return document.getName().contains(name);
                    }
                    return true;
                })
                .filter(document -> {
                    if (extension != null) {
                        return document.getExtension().contains(extension);
                    }
                    return true;
                })
                .filter(document -> {
                    if (fileSize != null) {
                        return document.getSize().equals(fileSize);
                    }
                    return true;
                })
                .filter(document -> {
                    if (mimeType != null) {
                        return document.getMimeType().contains(mimeType);
                    }
                    return true;
                })
                .toList();
        return new PageImpl<>(list, pageable, all.getTotalElements());
    }

    @Transactional
    public Document save(MultipartFile file, String name, String description) {
        String extension = getExtension(file);
        if (documentRepository.existsByNameAndExtensionAndSizeAndMimeType(name, extension, file.getSize(), file.getContentType())) {
            throw new IllegalStateException("There is already a document with the same parameters");
        }
        UUID uuid = UUID.randomUUID();
        saveFile(file, extension, uuid);
        Document document = Document.builder()
                .id(uuid)
                .name(name)
                .description(description)
                .size(file.getSize())
                .createdDate(Date.valueOf(LocalDate.now()))
                .extension(extension)
                .mimeType(file.getContentType())
                .build();
        return documentRepository.save(document);
    }

    private void saveFile(MultipartFile file, String extension, UUID uuid) {
        String name = uuid.toString() + "." + extension;
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

    @Transactional(readOnly = true)
    public Document findById(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }

    @Transactional
    public Document updateDocument(UUID id, String name, String description, MultipartFile file) {
        Document document = findById(id);
        String extension = getExtension(file);
        String filename = id.toString() + "." + extension;
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
        saveFile(file, extension, id);
        document.setName(name);
        document.setDescription(description);
        document.setExtension(extension);
        document.setSize(file.getSize());
        document.setMimeType(file.getContentType());
        document.setUpdatedDate(Date.valueOf(LocalDate.now()));
        return document;
    }

    @Transactional
    public void delete(UUID id) {
        Document document = findById(id);
        String name = id.toString() + "." + document.getExtension();
        documentRepository.delete(document);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(name)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private String getExtension(MultipartFile file) {
        try {
            return Objects
                    .requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Bad extension");
        }
    }

    @Transactional(readOnly = true)
    public InputStreamResource getDocument(String filename) {
        resolveFilename(filename);
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

    @Transactional(readOnly = true)
    public void resolveFilename(String filename) {
        String[] fileInfo = filename.split("\\.");
        Document document = findById(UUID.fromString(fileInfo[0]));
        if (!document.getExtension().equals(fileInfo[1])) {
            throw new IllegalStateException("Wrong file format");
        }
    }
}
