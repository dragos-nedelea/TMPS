package tmps.portfolio.strategy;

import tmps.portfolio.domain.Asset;
import tmps.portfolio.domain.StockAsset;
import tmps.portfolio.domain.ValuationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * [SOLID: OCP & SRP]
 * Concrete implementation of ValuationStrategy specifically for public equities (Stocks).
 * Calculates fair value as: (shares * currentMarketPrice) + (simulated annual dividend payout).
 */
public class StockValuationStrategy implements ValuationStrategy {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public boolean supports(Asset asset) {
        return asset instanceof StockAsset;
    }

    @Override
    public ValuationResult evaluate(Asset asset) {
        if (!(asset instanceof StockAsset stock)) {
            throw new IllegalArgumentException("StockValuationStrategy only supports StockAsset instances");
        }

        BigDecimal shares = BigDecimal.valueOf(stock.shares());
        BigDecimal baseMarketValue = stock.currentMarketPrice().multiply(shares);

        // Simulated 1-year forward dividend return: baseMarketValue * (yield / 100)
        BigDecimal dividendReturn = baseMarketValue
                .multiply(stock.annualDividendYieldPercent())
                .divide(HUNDRED, 4, RoundingMode.HALF_UP);

        BigDecimal fairValue = baseMarketValue.add(dividendReturn).setScale(2, RoundingMode.HALF_UP);

        String summary = String.format(
                "%d shares @ $%s = $%s base + $%s projected dividend (%.2f%% yield)",
                stock.shares(),
                stock.currentMarketPrice().setScale(2, RoundingMode.HALF_UP),
                baseMarketValue.setScale(2, RoundingMode.HALF_UP),
                dividendReturn.setScale(2, RoundingMode.HALF_UP),
                stock.annualDividendYieldPercent().doubleValue()
        );

        return new ValuationResult(stock, fairValue, strategyName(), summary);
    }

    @Override
    public String strategyName() {
        return "Dividend-Adjusted Market Value Model";
    }
}
