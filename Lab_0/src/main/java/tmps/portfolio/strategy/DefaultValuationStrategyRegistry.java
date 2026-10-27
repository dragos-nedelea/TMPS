package tmps.portfolio.strategy;

import tmps.portfolio.domain.Asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * [SOLID: OCP & DIP]
 * DefaultValuationStrategyRegistry holds registered ValuationStrategy instances
 * and implements ValuationStrategyProvider to satisfy the DIP contract.
 *
 * New strategies can be registered at runtime without modifying this class or any consumers.
 */
public class DefaultValuationStrategyRegistry implements ValuationStrategyProvider {

    private final List<ValuationStrategy> strategies = new ArrayList<>();

    public DefaultValuationStrategyRegistry register(ValuationStrategy strategy) {
        Objects.requireNonNull(strategy, "Strategy to register cannot be null");
        this.strategies.add(strategy);
        return this;
    }

    @Override
    public ValuationStrategy findStrategy(Asset asset) {
        Objects.requireNonNull(asset, "Asset cannot be null");

        for (ValuationStrategy strategy : strategies) {
            if (strategy.supports(asset)) {
                return strategy;
            }
        }

        throw new UnsupportedOperationException(
                "No valuation strategy registered capable of evaluating asset: "
                        + asset.name() + " [Type: " + asset.assetType() + ", Class: " + asset.getClass().getSimpleName() + "]"
        );
    }
}
