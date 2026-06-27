package com.sushank.ems.repository;

import com.sushank.ems.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
    List<Notification> findByEmployeeIdAndReadOrderByCreatedAtDesc(String employeeId, boolean read);
}
