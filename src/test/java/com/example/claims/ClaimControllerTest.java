package com.example.claims;

import com.example.claims.claim.ClaimController;
import com.example.claims.claim.ClaimService;
import com.example.claims.claim.ClaimStatus;
import com.example.claims.claim.dto.ClaimDto;
import com.example.claims.claim.dto.CreateClaimRequest;
import com.example.claims.common.exception.GlobalExceptionHandler;
import com.example.claims.common.exception.InvalidStateTransitionException;
import com.example.claims.policy.PolicyType;
import com.example.claims.security.CustomUserDetailsService;
import com.example.claims.security.JwtAuthenticationEntryPoint;
import com.example.claims.security.JwtTokenProvider;
import com.example.claims.security.SecurityConfig;
import com.example.claims.user.Role;
import com.example.claims.user.User;
import com.example.claims.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ClaimController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private User testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = User.builder()
                .id(10L)
                .email("test.customer@example.com")
                .passwordHash("hashed")
                .fullName("Test Customer")
                .role(Role.CUSTOMER)
                .build();

        given(userRepository.findById(any())).willReturn(Optional.of(testCustomer));
        given(userRepository.findByEmail(any())).willReturn(Optional.of(testCustomer));
    }

    @Test
    @WithMockUser(username = "test.customer@example.com", roles = "CUSTOMER")
    @DisplayName("POST /api/v1/claims creates claim and returns HTTP 201 Created")
    void createClaim_ShouldReturn201() throws Exception {
        CreateClaimRequest request = CreateClaimRequest.builder()
                .policyId(1L)
                .incidentDate(LocalDate.of(2026, 3, 10))
                .description("Accidental injury medical expense")
                .claimedAmount(new BigDecimal("1500.00"))
                .build();

        ClaimDto responseDto = ClaimDto.builder()
                .id(100L)
                .claimNumber("CLM-2026-123456")
                .incidentDate(LocalDate.of(2026, 3, 10))
                .description("Accidental injury medical expense")
                .claimedAmount(new BigDecimal("1500.00"))
                .status(ClaimStatus.DRAFT)
                .policyId(1L)
                .policyNumber("POL-100")
                .policyType(PolicyType.HEALTH)
                .holderId(10L)
                .holderName("Test Customer")
                .version(0L)
                .createdAt(Instant.now())
                .build();

        given(claimService.createClaim(any(CreateClaimRequest.class), any(User.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.claimNumber").value("CLM-2026-123456"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.claimedAmount").value(1500.00));
    }

    @Test
    @WithMockUser(username = "test.customer@example.com", roles = "CUSTOMER")
    @DisplayName("Invalid state transition maps to HTTP 409 Conflict with standardized error body")
    void invalidStateTransition_ShouldReturn409Conflict() throws Exception {
        given(claimService.submitClaim(eq(100L), any(User.class)))
                .willThrow(new InvalidStateTransitionException("Invalid state transition from APPROVED to SUBMITTED"));

        mockMvc.perform(post("/api/v1/claims/100/submit"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"))
                .andExpect(jsonPath("$.message").value("Invalid state transition from APPROVED to SUBMITTED"));
    }

    @Test
    @WithMockUser(username = "test.customer@example.com", roles = "CUSTOMER")
    @DisplayName("Validation failure returns HTTP 400 Bad Request with fieldErrors")
    void invalidRequestBody_ShouldReturn400BadRequest() throws Exception {
        CreateClaimRequest invalidRequest = CreateClaimRequest.builder()
                .policyId(null)
                .description("")
                .claimedAmount(new BigDecimal("-50.00"))
                .build();

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
