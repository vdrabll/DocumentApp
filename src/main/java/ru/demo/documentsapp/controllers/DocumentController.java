package ru.demo.documentsapp.controllers;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.demo.documentsapp.entities.Document;
import ru.demo.documentsapp.services.DocumentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/documents")
    public Page<Document> getDocuments(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "extension", required = false) String extension,
                                       @RequestParam(value = "fileSize", required = false) Long fileSize,
                                       @RequestParam(value = "mimeType", required = false) String mimeType,
                                       @ParameterObject Pageable pageable) {
        return documentService.findAll(name, extension, fileSize, mimeType, pageable);
    }

    @GetMapping("/documents/{id}")
    public Document getDocument(@PathVariable UUID id) {
        return documentService.findById(id);
    }

    @PostMapping(value = "/document", consumes = "multipart/form-data")
    public Document uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description) {
        return documentService.save(file, name, description);
    }

    @PutMapping(value = "/document/{id}")
    public Document updateDocument(@PathVariable("id") UUID id,
                                   @RequestParam("name") String name,
                                   @RequestParam("description") String description,
                                   @RequestPart("file") MultipartFile document) {
        return documentService.updateDocument(id, name, description, document);
    }

    @GetMapping("/document/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadDocumentById(@PathVariable("filename") String filename) {
        InputStreamResource file = documentService.getDocument(filename);
        String header = "attachment; filename=" + "\"" + filename + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, header)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @DeleteMapping("/document/{id}")
    public void deletePictureById(@PathVariable("id") UUID id) {
        documentService.delete(id);
    }
}
