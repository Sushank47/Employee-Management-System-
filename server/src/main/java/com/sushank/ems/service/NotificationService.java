package com.sushank.ems.service;

import com.sushank.ems.entity.Notification;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendNotification(String employeeId, String message) {
        Notification notification = Notification.builder()
                .employeeId(employeeId)
                .message(message)
                .read(false)
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForEmployee(String employeeId) {
        return notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    public List<Notification> getUnreadNotificationsForEmployee(String employeeId) {
        return notificationRepository.findByEmployeeIdAndReadOrderByCreatedAtDesc(employeeId, false);
    }

    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String employeeId) {
        List<Notification> unread = notificationRepository.findByEmployeeIdAndReadOrderByCreatedAtDesc(employeeId, false);
        for (Notification notification : unread) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}
