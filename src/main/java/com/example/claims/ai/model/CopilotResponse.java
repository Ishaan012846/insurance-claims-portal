package com.example.claims.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopilotResponse {
    private String answer;
    private List<PolicyClause> retrievedClauses;
    private String modelUsed;
    private Double confidenceScore;
}
