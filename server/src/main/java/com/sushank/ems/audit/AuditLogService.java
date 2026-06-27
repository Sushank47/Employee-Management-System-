package com.sushank.ems.audit;

import com.sushank.ems.audit.AuditLog;
import com.sushank.ems.audit.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String action, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String performedBy = (authentication != null && authentication.isAuthenticated()) 
                ? authentication.getName() 
                : "SYSTEM";

        AuditLog log = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .details(details)
                .timestamp(Instant.now())
                .build();

        auditLogRepository.save(log);
    }

    public void log(String action, String performedBy, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .details(details)
                .timestamp(Instant.now())
                .build();

        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
