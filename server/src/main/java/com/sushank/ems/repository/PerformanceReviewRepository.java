package com.sushank.ems.repository;

import com.sushank.ems.entity.PerformanceReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PerformanceReviewRepository extends MongoRepository<PerformanceReview, String> {
    List<PerformanceReview> findByEmployeeId(String employeeId);
}
