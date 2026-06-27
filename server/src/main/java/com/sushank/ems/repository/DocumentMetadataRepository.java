package com.sushank.ems.repository;

import com.sushank.ems.entity.DocumentMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentMetadataRepository extends MongoRepository<DocumentMetadata, String> {
    List<DocumentMetadata> findByEmployeeId(String employeeId);
    Optional<DocumentMetadata> findByFileId(String fileId);
}
