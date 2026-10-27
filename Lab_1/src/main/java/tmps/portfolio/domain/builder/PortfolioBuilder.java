package tmps.portfolio.domain.builder;

import tmps.portfolio.domain.models.Asset;
import tmps.portfolio.domain.models.Portfolio;
import tmps.portfolio.domain.models.Portfolio.RiskProfile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Builder Pattern: Assembles complex Portfolio domain objects.
 *
 * Problem Solved:
 * Avoids telescopic constructors and allows step-by-step assembly of complex financial portfolios
 * with full invariant validation (email format, non-negative cash, non-empty identifiers).
 */
public class PortfolioBuilder {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private String id;
    private String investorName;
    private String investorEmail;
    private RiskProfile riskProfile = RiskProfile.BALANCED;
    private String baseCurrency = "USD";
    private BigDecimal cashBalance = BigDecimal.ZERO;
    private final List<Asset> assets = new ArrayList<>();
    private final Set<String> tags = new HashSet<>();
    private Instant createdAt = Instant.now();

    public PortfolioBuilder() {
    }

    public PortfolioBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public PortfolioBuilder withInvestor(String name, String email) {
        this.investorName = name;
        this.investorEmail = email;
        return this;
    }

    public PortfolioBuilder withRiskProfile(RiskProfile riskProfile) {
        this.riskProfile = riskProfile;
        return this;
    }

    public PortfolioBuilder withBaseCurrency(String currency) {
        this.baseCurrency = currency;
        return this;
    }

    public PortfolioBuilder withCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
        return this;
    }

    public PortfolioBuilder addAsset(Asset asset) {
        if (asset != null) {
            this.assets.add(asset);
        }
        return this;
    }

    public PortfolioBuilder addAssets(Collection<? extends Asset> newAssets) {
        if (newAssets != null) {
            for (Asset a : newAssets) {
                if (a != null) {
                    this.assets.add(a);
                }
            }
        }
        return this;
    }

    public PortfolioBuilder addTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            this.tags.add(tag.trim().toLowerCase());
        }
        return this;
    }

    public PortfolioBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Invariant validation and final object assembly.
     */
    public Portfolio build() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalStateException("Cannot build Portfolio: Portfolio ID is mandatory.");
        }
        if (investorName == null || investorName.trim().isEmpty()) {
            throw new IllegalStateException("Cannot build Portfolio: Investor name is mandatory.");
        }
        if (investorEmail == null || !EMAIL_PATTERN.matcher(investorEmail).matches()) {
            throw new IllegalStateException("Cannot build Portfolio: Valid investor email is required (given: " + investorEmail + ").");
        }
        if (cashBalance == null || cashBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Cannot build Portfolio: Cash balance cannot be negative.");
        }
        if (baseCurrency == null || baseCurrency.trim().length() != 3) {
            throw new IllegalStateException("Cannot build Portfolio: Base currency must be a valid 3-letter ISO code.");
        }

        return new Portfolio(
                id.trim(),
                investorName.trim(),
                investorEmail.trim(),
                riskProfile,
                baseCurrency.trim().toUpperCase(),
                cashBalance,
                assets,
                tags,
                createdAt
        );
    }
}
