package tmps.portfolio.strategy;

import tmps.portfolio.domain.Asset;
import tmps.portfolio.domain.ValuationResult;

/**
 * [SOLID: OCP - Open/Closed Principle]
 * ValuationStrategy defines the contract for asset pricing algorithms.
 * By isolating valuation algorithms behind this strategy interface, the system is:
 * - OPEN for extension: New asset types and financial pricing models can be plugged in.
 * - CLOSED for modification: Existing valuation strategies and the orchestrating engine
 *   never need to be touched or recompiled when new asset classes are introduced.
 */
public interface ValuationStrategy {

    /**
     * Identifies whether this strategy is capable of appraising the provided asset.
     */
    boolean supports(Asset asset);

    /**
     * Executes the financial valuation algorithm on the specified asset.
     *
     * @param asset Domain asset to value
     * @return ValuationResult containing the calculated fair value and financial rationale
     */
    ValuationResult evaluate(Asset asset);

    /**
     * Returns a human-readable identifier of the pricing model.
     */
    String strategyName();
}
