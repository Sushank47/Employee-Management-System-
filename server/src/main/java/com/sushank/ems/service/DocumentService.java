package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.entity.DocumentMetadata;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.DocumentMetadataRepository;
import com.sushank.ems.repository.EmployeeRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private DocumentMetadataRepository documentMetadataRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    public String uploadDocument(String employeeId, String documentType, MultipartFile file) throws IOException {
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(), 
                file.getOriginalFilename(), 
                file.getContentType()
        );

        DocumentMetadata metadata = DocumentMetadata.builder()
                .fileId(fileId.toString())
                .employeeId(employeeId)
                .documentType(documentType.toUpperCase())
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .uploadedAt(Instant.now())
                .build();

        documentMetadataRepository.save(metadata);
        
        auditLogService.log("DOCUMENT_UPLOAD", employeeId, 
                "Uploaded document of type " + documentType + " name: " + file.getOriginalFilename());

        return fileId.toString();
    }

    public GridFsResource downloadDocument(String fileId) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
        if (file == null) {
            throw new ResourceNotFoundException("Document not found with GridFS ID: " + fileId);
        }
        return gridFsTemplate.getResource(file);
    }

    public DocumentMetadata getMetadataByFileId(String fileId) {
        return documentMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Document metadata not found for ID: " + fileId));
    }

    public List<DocumentMetadata> getDocumentsByEmployee(String employeeId) {
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        return documentMetadataRepository.findByEmployeeId(employeeId);
    }

    public void deleteDocument(String fileId) {
        DocumentMetadata metadata = documentMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Document metadata not found for ID: " + fileId));

        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
        documentMetadataRepository.delete(metadata);

        auditLogService.log("DOCUMENT_DELETE", metadata.getEmployeeId(), 
                "Deleted document of type " + metadata.getDocumentType() + " name: " + metadata.getFileName());
    }
}
