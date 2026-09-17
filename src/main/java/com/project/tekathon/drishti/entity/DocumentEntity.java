package com.project.tekathon.drishti.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class DocumentEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String documentId;

    @Column(nullable = false)
    private String caseId;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String title;

    @Lob
    private String text;

    @Column(nullable = false)
    private String source;

    private String language;

    private String fileName;

    private String contentType;

    @Lob
    private String nlpResultJson;
}
