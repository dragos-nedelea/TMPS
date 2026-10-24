package tmps.portfolio.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * [SOLID: SRP - Single Responsibility Principle]
 * ValuationResult represents the evaluated fair value and financial rationale for a single asset.
 * It is a pure data carrier.
 */
public record ValuationResult(
        Asset asset,
        BigDecimal fairValue,
        String valuationStrategyName,
        String calculationSummary
) {
    public ValuationResult {
        Objects.requireNonNull(asset, "Asset cannot be null");
        Objects.requireNonNull(fairValue, "Fair value cannot be null");
        Objects.requireNonNull(valuationStrategyName, "Valuation strategy name cannot be null");
        Objects.requireNonNull(calculationSummary, "Calculation summary cannot be null");
    }
}
