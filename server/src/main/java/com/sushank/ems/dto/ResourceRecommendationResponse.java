package com.sushank.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRecommendationResponse {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> skills;
    private double skillMatchPercentage;
    private double availabilityScore;
    private boolean hasOverlappingLeave;
    private double overallScore;
}
