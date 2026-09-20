package com.example.claims.claim.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignHandlerRequest {

    @NotNull(message = "Handler ID is required")
    private Long handlerId;
}
