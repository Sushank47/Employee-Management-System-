package com.sushank.ems.controller;

import com.sushank.ems.entity.Notification;
import com.sushank.ems.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or (#employeeId == authentication.principal.employeeId)")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String employeeId) {
        List<Notification> list = notificationService.getNotificationsForEmployee(employeeId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/employee/{employeeId}/unread")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or (#employeeId == authentication.principal.employeeId)")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable String employeeId) {
        List<Notification> list = notificationService.getUnreadNotificationsForEmployee(employeeId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/employee/{employeeId}/read-all")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String employeeId) {
        notificationService.markAllAsRead(employeeId);
        return ResponseEntity.ok().build();
    }
}
