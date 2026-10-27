package tmps.portfolio.domain.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Concrete representation of a Peer-to-Peer consumer loan / fixed debt asset.
 */
public class P2PLoanAsset implements Asset {

    private final String id;
    private final String borrowerId;
    private final String platform;
    private final BigDecimal principal;
    private final BigDecimal interestRatePercent;
    private final int termMonths;
    private final BigDecimal defaultProbabilityPercent;
    private final String currency;

    public P2PLoanAsset(
            String id,
            String borrowerId,
            String platform,
            BigDecimal principal,
            BigDecimal interestRatePercent,
            int termMonths,
            BigDecimal defaultProbabilityPercent,
            String currency
    ) {
        this.id = Objects.requireNonNull(id, "Asset ID cannot be null");
        this.borrowerId = Objects.requireNonNull(borrowerId, "Borrower ID cannot be null");
        this.platform = Objects.requireNonNull(platform, "Platform cannot be null");
        this.principal = validateNonNegative(principal, "Principal");
        this.interestRatePercent = validateNonNegative(interestRatePercent, "Interest rate");
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Term months must be positive.");
        }
        this.termMonths = termMonths;
        this.defaultProbabilityPercent = Objects.requireNonNullElse(defaultProbabilityPercent, BigDecimal.ZERO);
        this.currency = Objects.requireNonNullElse(currency, "USD").toUpperCase().trim();
    }

    private static BigDecimal validateNonNegative(BigDecimal val, String name) {
        if (val == null || val.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must be non-negative.");
        }
        return val;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return "P2P Loan #" + id + " (" + platform + " - Ref: " + borrowerId + ")";
    }

    @Override
    public AssetType getType() {
        return AssetType.P2P_DEBT;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public String getPlatform() {
        return platform;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BigDecimal getInterestRatePercent() {
        return interestRatePercent;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public BigDecimal getDefaultProbabilityPercent() {
        return defaultProbabilityPercent;
    }

    @Override
    public BigDecimal calculateFairValue() {
        // Principal + simple interest over term - expected default loss haircut
        BigDecimal annualInterest = principal.multiply(interestRatePercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal totalInterest = annualInterest.multiply(BigDecimal.valueOf(termMonths)).divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
        BigDecimal totalReceivable = principal.add(totalInterest);

        BigDecimal riskHaircut = totalReceivable.multiply(defaultProbabilityPercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal netValue = totalReceivable.subtract(riskHaircut);

        return (netValue.compareTo(BigDecimal.ZERO) > 0 ? netValue : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Prototype Pattern Implementation:
     * Produces a deep copy of this debt asset.
     */
    @Override
    public P2PLoanAsset cloneAsset() {
        return new P2PLoanAsset(
                this.id,
                this.borrowerId,
                this.platform,
                this.principal,
                this.interestRatePercent,
                this.termMonths,
                this.defaultProbabilityPercent,
                this.currency
        );
    }

    public P2PLoanAsset cloneWithBorrower(String newId, String newBorrowerId, BigDecimal newPrincipal) {
        return new P2PLoanAsset(
                newId,
                newBorrowerId,
                this.platform,
                newPrincipal,
                this.interestRatePercent,
                this.termMonths,
                this.defaultProbabilityPercent,
                this.currency
        );
    }

    @Override
    public String toString() {
        return String.format("P2PLoanAsset[%s | %s via %s | Principal: %s %s @ %s%% for %d mo | Value: %s %s]",
                id, borrowerId, platform, principal, currency, interestRatePercent, termMonths, calculateFairValue(), currency);
    }
}
