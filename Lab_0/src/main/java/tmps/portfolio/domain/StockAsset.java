package tmps.portfolio.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * [SOLID: SRP - Single Responsibility Principle]
 * StockAsset is an immutable domain model (Java Record).
 * Responsibility: Holding stock asset state and validating domain invariants.
 * It deliberately does NOT calculate its own market valuation or serialize itself.
 */
public record StockAsset(
        String id,
        String symbol,
        String companyName,
        int shares,
        BigDecimal currentMarketPrice,
        BigDecimal annualDividendYieldPercent
) implements Asset {

    public StockAsset {
        Objects.requireNonNull(id, "Asset ID cannot be null");
        Objects.requireNonNull(symbol, "Stock symbol cannot be null");
        Objects.requireNonNull(companyName, "Company name cannot be null");
        Objects.requireNonNull(currentMarketPrice, "Market price cannot be null");
        Objects.requireNonNull(annualDividendYieldPercent, "Dividend yield cannot be null");

        if (shares <= 0) {
            throw new IllegalArgumentException("Shares count must be strictly positive, got: " + shares);
        }
        if (currentMarketPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Market price cannot be negative, got: " + currentMarketPrice);
        }
        if (annualDividendYieldPercent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Dividend yield cannot be negative, got: " + annualDividendYieldPercent);
        }
    }

    @Override
    public String name() {
        return companyName + " (" + symbol + ")";
    }

    @Override
    public String assetType() {
        return "PUBLIC_EQUITY";
    }
}
