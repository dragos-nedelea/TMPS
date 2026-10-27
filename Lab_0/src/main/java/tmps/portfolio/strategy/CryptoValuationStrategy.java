package tmps.portfolio.strategy;

import tmps.portfolio.domain.Asset;
import tmps.portfolio.domain.CryptoAsset;
import tmps.portfolio.domain.ValuationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * [SOLID: OCP - Open/Closed Principle]
 * This strategy proves OCP:
 * It was added WITHOUT altering any existing code in StockValuationStrategy,
 * P2PLoanValuationStrategy, or PortfolioService.
 *
 * Pricing Model:
 * Gross Value = UnitsHeld * SpotPriceUsd
 * Fair Value = Gross Value - EstimatedGasFeeUsd (liquidation cost)
 */
public class CryptoValuationStrategy implements ValuationStrategy {

    @Override
    public boolean supports(Asset asset) {
        return asset instanceof CryptoAsset;
    }

    @Override
    public ValuationResult evaluate(Asset asset) {
        if (!(asset instanceof CryptoAsset crypto)) {
            throw new IllegalArgumentException("CryptoValuationStrategy only supports CryptoAsset instances");
        }

        BigDecimal grossValue = crypto.unitsHeld().multiply(crypto.spotPriceUsd());
        BigDecimal netValue = grossValue.subtract(crypto.estimatedGasFeeUsd()).setScale(2, RoundingMode.HALF_UP);

        // Ensure fair value does not go below zero due to fees
        BigDecimal fairValue = netValue.max(BigDecimal.ZERO);

        String summary = String.format(
                "%s %s @ $%s = gross $%s, minus network fee -$%s",
                crypto.unitsHeld().stripTrailingZeros().toPlainString(),
                crypto.tokenSymbol(),
                crypto.spotPriceUsd().setScale(2, RoundingMode.HALF_UP),
                grossValue.setScale(2, RoundingMode.HALF_UP),
                crypto.estimatedGasFeeUsd().setScale(2, RoundingMode.HALF_UP)
        );

        return new ValuationResult(crypto, fairValue, strategyName(), summary);
    }

    @Override
    public String strategyName() {
        return "Spot Market Net-of-Gas-Fee Model";
    }
}
