package com.example.claims;

import com.example.claims.claim.ClaimStateMachine;
import com.example.claims.claim.ClaimStatus;
import com.example.claims.common.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimStateMachineTest {

    private static final Set<String> VALID_PAIRS = Set.of(
            "DRAFT->SUBMITTED",
            "SUBMITTED->UNDER_REVIEW",
            "UNDER_REVIEW->INFO_REQUESTED",
            "UNDER_REVIEW->APPROVED",
            "UNDER_REVIEW->REJECTED",
            "INFO_REQUESTED->UNDER_REVIEW",
            "APPROVED->SETTLED"
    );

    static List<Arguments> allStatusPairs() {
        List<Arguments> pairs = new ArrayList<>();
        for (ClaimStatus from : ClaimStatus.values()) {
            for (ClaimStatus to : ClaimStatus.values()) {
                boolean expectedValid = VALID_PAIRS.contains(from.name() + "->" + to.name());
                pairs.add(Arguments.of(from, to, expectedValid));
            }
        }
        return pairs;
    }

    @ParameterizedTest(name = "[{index}] Transition {0} -> {1} (Expected Valid: {2})")
    @MethodSource("allStatusPairs")
    @DisplayName("Exhaustive state machine matrix test for all 49 (fromStatus, toStatus) pairs")
    void testFullStateTransitionMatrix(ClaimStatus from, ClaimStatus to, boolean expectedValid) {
        boolean isAllowed = ClaimStateMachine.isTransitionAllowed(from, to);
        assertThat(isAllowed).isEqualTo(expectedValid);

        if (expectedValid) {
            // Should complete cleanly without exception
            ClaimStateMachine.validateTransition(from, to);
        } else {
            // Should throw InvalidStateTransitionException
            assertThatThrownBy(() -> ClaimStateMachine.validateTransition(from, to))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining(String.format("Invalid state transition from %s to %s", from, to));
        }
    }
}
