package com.example.claims.claim;

import com.example.claims.common.exception.InvalidStateTransitionException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class ClaimStateMachine {

    private static final Map<ClaimStatus, Set<ClaimStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ClaimStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ClaimStatus.DRAFT, Set.of(ClaimStatus.SUBMITTED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.SUBMITTED, Set.of(ClaimStatus.UNDER_REVIEW));
        ALLOWED_TRANSITIONS.put(ClaimStatus.UNDER_REVIEW, Set.of(ClaimStatus.INFO_REQUESTED, ClaimStatus.APPROVED, ClaimStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.INFO_REQUESTED, Set.of(ClaimStatus.UNDER_REVIEW));
        ALLOWED_TRANSITIONS.put(ClaimStatus.APPROVED, Set.of(ClaimStatus.SETTLED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.REJECTED, Set.of());
        ALLOWED_TRANSITIONS.put(ClaimStatus.SETTLED, Set.of());
    }

    public static boolean isTransitionAllowed(ClaimStatus from, ClaimStatus to) {
        if (from == null || to == null) {
            return false;
        }
        Set<ClaimStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        return allowed.contains(to);
    }

    public static void validateTransition(ClaimStatus from, ClaimStatus to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException(
                    String.format("Invalid state transition from %s to %s", from, to)
            );
        }
    }
}
