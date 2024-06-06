package ru.demo.documentsapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.demo.documentsapp.entities.Document;

import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Boolean existsByNameAndExtensionAndSizeAndMimeType(String name, String extension, Long size, String mimeType);
}
