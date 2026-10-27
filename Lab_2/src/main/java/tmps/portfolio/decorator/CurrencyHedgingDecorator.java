package tmps.portfolio.decorator;

import tmps.portfolio.composite.PortfolioComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * [DECORATOR - CONCRETE DECORATOR]
 * Dynamically applies currency / foreign exchange (FX) hedging protection cost.
 */
public class CurrencyHedgingDecorator extends ValuationDecorator {

    private final BigDecimal hedgePremiumPercentage;
    private final String targetCurrency;

    public CurrencyHedgingDecorator(
            PortfolioComponent decoratedComponent,
            BigDecimal hedgePremiumPercentage,
            String targetCurrency
    ) {
        super(decoratedComponent);
        if (hedgePremiumPercentage == null || hedgePremiumPercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Hedge premium percentage cannot be negative: " + hedgePremiumPercentage);
        }
        this.hedgePremiumPercentage = hedgePremiumPercentage;
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getHedgePremiumPercentage() {
        return hedgePremiumPercentage;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal calculateHedgeCost() {
        BigDecimal base = decoratedComponent.calculateFairValue();
        return base.multiply(hedgePremiumPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateFairValue() {
        BigDecimal base = decoratedComponent.calculateFairValue();
        BigDecimal cost = calculateHedgeCost();
        return base.subtract(cost).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void printHierarchy(String indent, StringBuilder out) {
        out.append(indent)
                .append("🌐 [DECORATOR: FX Hedged into ")
                .append(targetCurrency)
                .append(" @ ")
                .append(hedgePremiumPercentage)
                .append("%] Pre-Hedge: ")
                .append(decoratedComponent.calculateFairValue())
                .append(" | Hedge Cost: -")
                .append(calculateHedgeCost())
                .append(" | Net Hedged: ")
                .append(calculateFairValue())
                .append("\n");

        decoratedComponent.printHierarchy(indent + "  ", out);
    }
}
