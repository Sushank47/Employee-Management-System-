package com.sushank.ems.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentMetadata {
    @Id
    private String id;

    private String fileId; // GridFS ObjectId as String
    private String employeeId; // references Employee.employeeId
    private String documentType; // RESUME, OFFER_LETTER, PAN, AADHAAR, CERTIFICATE
    private String fileName;
    private String contentType;

    @CreatedDate
    private Instant uploadedAt;
}
