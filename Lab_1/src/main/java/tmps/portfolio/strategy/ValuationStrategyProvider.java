package tmps.portfolio.strategy;

import tmps.portfolio.domain.Asset;

/**
 * [SOLID: DIP - Dependency Inversion Principle & OCP]
 * ValuationStrategyProvider is the abstraction used by high-level services
 * to look up the appropriate calculation strategy dynamically.
 * High-level orchestration code depends on this abstraction, NEVER on concrete strategies.
 */
public interface ValuationStrategyProvider {

    /**
     * Resolves an appropriate valuation strategy capable of evaluating the given asset.
     *
     * @param asset The asset to be appraised
     * @return The matching ValuationStrategy
     * @throws UnsupportedOperationException if no registered strategy can handle the asset
     */
    ValuationStrategy findStrategy(Asset asset);
}
