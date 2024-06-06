package ru.demo.documentsapp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.UUID;

@Entity(name = "document")
@Table
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    @Id
    private UUID id;
    private String name;
    private String description;
    private Date createdDate;
    private String extension;
    private Long size;
    private String mimeType;
    private Date updatedDate;
}
