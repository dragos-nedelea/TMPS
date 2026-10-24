package tmps.portfolio.domain;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * [SOLID: SRP - Single Responsibility Principle]
 * Portfolio represents the container of owned assets.
 * Responsibility: Tracking the portfolio metadata and maintaining the collection of assets.
 * Notice: It does NOT calculate its total value or print reports. That responsibility belongs
 * to service and exporter components.
 */
public record Portfolio(
        String id,
        String investorName,
        List<Asset> assets
) {
    public Portfolio {
        Objects.requireNonNull(id, "Portfolio ID cannot be null");
        Objects.requireNonNull(investorName, "Investor name cannot be null");
        Objects.requireNonNull(assets, "Assets list cannot be null");
        assets = List.copyOf(assets); // Ensure immutability
    }

    public List<Asset> assets() {
        return Collections.unmodifiableList(assets);
    }
}
