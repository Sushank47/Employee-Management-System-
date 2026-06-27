package com.sushank.ems.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ResourceRecommendationRequest {
    private String projectId;
    private List<String> requiredSkills;
    private LocalDate startDate;
    private LocalDate endDate;
}
