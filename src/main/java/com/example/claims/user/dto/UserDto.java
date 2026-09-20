package com.example.claims.user.dto;

import com.example.claims.user.Role;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Instant createdAt;
}
