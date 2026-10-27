package tmps.portfolio.domain.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Complex domain entity representing an investor's overall portfolio.
 * Constructed via the PortfolioBuilder pattern to enforce immutability and invariant safety.
 */
public class Portfolio {

    public enum RiskProfile {
        CONSERVATIVE,
        BALANCED,
        GROWTH,
        AGGRESSIVE
    }

    private final String id;
    private final String investorName;
    private final String investorEmail;
    private final RiskProfile riskProfile;
    private final String baseCurrency;
    private final BigDecimal cashBalance;
    private final List<Asset> assets;
    private final Set<String> tags;
    private final Instant createdAt;

    /**
     * Constructor for Portfolio entity. PortfolioBuilder is the recommended creational mechanism.
     */
    public Portfolio(
            String id,
            String investorName,
            String investorEmail,
            RiskProfile riskProfile,
            String baseCurrency,
            BigDecimal cashBalance,
            List<Asset> assets,
            Set<String> tags,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Portfolio ID cannot be null");
        this.investorName = Objects.requireNonNull(investorName, "Investor name cannot be null");
        this.investorEmail = Objects.requireNonNull(investorEmail, "Investor email cannot be null");
        this.riskProfile = Objects.requireNonNullElse(riskProfile, RiskProfile.BALANCED);
        this.baseCurrency = Objects.requireNonNullElse(baseCurrency, "USD").toUpperCase();
        this.cashBalance = Objects.requireNonNullElse(cashBalance, BigDecimal.ZERO);
        this.assets = Collections.unmodifiableList(new ArrayList<>(assets));
        this.tags = Collections.unmodifiableSet(new HashSet<>(tags));
        this.createdAt = Objects.requireNonNullElseGet(createdAt, Instant::now);
    }

    public String getId() {
        return id;
    }

    public String getInvestorName() {
        return investorName;
    }

    public String getInvestorEmail() {
        return investorEmail;
    }

    public RiskProfile getRiskProfile() {
        return riskProfile;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Aggregates fair market value across all owned assets.
     */
    public BigDecimal calculateAssetValuation() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Asset a : assets) {
            sum = sum.add(a.calculateFairValue());
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Total Net Worth = Total Asset Valuation + Cash Holdings.
     */
    public BigDecimal calculateNetWorth() {
        return calculateAssetValuation().add(cashBalance).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format(
                "Portfolio[ID=%s | Investor=%s <%s> | Risk=%s | Assets=%d | Cash=%s %s | NetWorth=%s %s]",
                id, investorName, investorEmail, riskProfile, assets.size(), cashBalance, baseCurrency, calculateNetWorth(), baseCurrency
        );
    }
}
