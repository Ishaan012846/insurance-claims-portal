package com.example.claims.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyClause {
    private String clauseId;
    private String policyType; // HEALTH, MOTOR, PROPERTY, ALL
    private String title;
    private String category; // COVERAGE, EXCLUSION, SUB_LIMIT, PROCEDURE
    private String content;
}
