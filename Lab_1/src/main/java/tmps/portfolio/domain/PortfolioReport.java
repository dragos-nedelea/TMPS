package tmps.portfolio.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * [SOLID: SRP - Single Responsibility Principle]
 * PortfolioReport encapsulates the consolidated output of the valuation engine.
 * It contains no presentation logic (no HTML, JSON, or console formatting).
 */
public record PortfolioReport(
        String portfolioId,
        String investorName,
        List<ValuationResult> items,
        BigDecimal totalValuation,
        Instant evaluatedAt
) {
    public PortfolioReport {
        Objects.requireNonNull(portfolioId, "Portfolio ID cannot be null");
        Objects.requireNonNull(investorName, "Investor name cannot be null");
        Objects.requireNonNull(items, "Items list cannot be null");
        Objects.requireNonNull(totalValuation, "Total valuation cannot be null");
        Objects.requireNonNull(evaluatedAt, "Evaluation timestamp cannot be null");
        items = List.copyOf(items);
    }

    public List<ValuationResult> items() {
        return Collections.unmodifiableList(items);
    }
}
