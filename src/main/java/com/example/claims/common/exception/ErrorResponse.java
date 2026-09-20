package com.example.claims.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @Builder.Default
    private Instant timestamp = Instant.now();
    private int status;
    private String code;
    private String message;
    @Builder.Default
    private List<FieldErrorDto> fieldErrors = new ArrayList<>();
}
