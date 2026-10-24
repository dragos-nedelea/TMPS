package tmps.portfolio.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * [SOLID: SRP - Single Responsibility Principle]
 * P2PLoanAsset represents an investment in a peer-to-peer consumer or business loan.
 * Responsibility: Holding loan state and enforcing domain integrity constraints.
 */
public record P2PLoanAsset(
        String id,
        String borrowerReference,
        BigDecimal principalRemaining,
        BigDecimal annualInterestRatePercent,
        int remainingMonths,
        BigDecimal defaultRiskProbabilityPercent
) implements Asset {

    public P2PLoanAsset {
        Objects.requireNonNull(id, "Asset ID cannot be null");
        Objects.requireNonNull(borrowerReference, "Borrower reference cannot be null");
        Objects.requireNonNull(principalRemaining, "Principal cannot be null");
        Objects.requireNonNull(annualInterestRatePercent, "Interest rate cannot be null");
        Objects.requireNonNull(defaultRiskProbabilityPercent, "Default risk cannot be null");

        if (principalRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal remaining must be positive, got: " + principalRemaining);
        }
        if (remainingMonths < 0) {
            throw new IllegalArgumentException("Remaining months cannot be negative, got: " + remainingMonths);
        }
        if (annualInterestRatePercent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative, got: " + annualInterestRatePercent);
        }
        if (defaultRiskProbabilityPercent.compareTo(BigDecimal.ZERO) < 0 ||
            defaultRiskProbabilityPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Default risk probability must be between 0 and 100%, got: " + defaultRiskProbabilityPercent);
        }
    }

    @Override
    public String name() {
        return "P2P Loan #" + id + " (Ref: " + borrowerReference + ")";
    }

    @Override
    public String assetType() {
        return "P2P_DEBT";
    }
}
