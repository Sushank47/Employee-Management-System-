package com.sushank.ems.controller;

import com.sushank.ems.dto.MessageResponse;
import com.sushank.ems.entity.DocumentMetadata;
import com.sushank.ems.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<MessageResponse> uploadDocument(
            @RequestParam String employeeId,
            @RequestParam String documentType,
            @RequestParam MultipartFile file) {
        try {
            String fileId = documentService.uploadDocument(employeeId, documentType, file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Document uploaded successfully. File ID: " + fileId));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to upload document: " + e.getMessage()));
        }
    }

    @GetMapping("/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable String fileId) throws IOException {
        GridFsResource resource = documentService.downloadDocument(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(new InputStreamResource(resource.getInputStream()));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or (#employeeId == authentication.principal.employeeId)")
    public ResponseEntity<List<DocumentMetadata>> getEmployeeDocuments(@PathVariable String employeeId) {
        List<DocumentMetadata> list = documentService.getDocumentsByEmployee(employeeId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<MessageResponse> deleteDocument(@PathVariable String fileId) {
        documentService.deleteDocument(fileId);
        return ResponseEntity.ok(new MessageResponse("Document deleted successfully"));
    }
}
