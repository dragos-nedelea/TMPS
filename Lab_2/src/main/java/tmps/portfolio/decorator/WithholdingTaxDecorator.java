package tmps.portfolio.decorator;

import tmps.portfolio.composite.PortfolioComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * [DECORATOR - CONCRETE DECORATOR]
 * Dynamically deducts regulatory Capital Gains Withholding Tax from the portfolio valuation.
 */
public class WithholdingTaxDecorator extends ValuationDecorator {

    private final BigDecimal taxRatePercentage;

    public WithholdingTaxDecorator(PortfolioComponent decoratedComponent, BigDecimal taxRatePercentage) {
        super(decoratedComponent);
        if (taxRatePercentage == null || taxRatePercentage.compareTo(BigDecimal.ZERO) < 0 || taxRatePercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Tax rate percentage must be between 0% and 100%: " + taxRatePercentage);
        }
        this.taxRatePercentage = taxRatePercentage;
    }

    public BigDecimal getTaxRatePercentage() {
        return taxRatePercentage;
    }

    public BigDecimal calculateTaxAmount() {
        BigDecimal gross = decoratedComponent.calculateFairValue();
        return gross.multiply(taxRatePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateFairValue() {
        BigDecimal gross = decoratedComponent.calculateFairValue();
        BigDecimal tax = calculateTaxAmount();
        return gross.subtract(tax).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void printHierarchy(String indent, StringBuilder out) {
        out.append(indent)
                .append("🛡️ [DECORATOR: Withholding Tax ")
                .append(taxRatePercentage)
                .append("%] Gross: ")
                .append(decoratedComponent.calculateFairValue())
                .append(" | Tax Deducted: -")
                .append(calculateTaxAmount())
                .append(" | Net: ")
                .append(calculateFairValue())
                .append("\n");

        decoratedComponent.printHierarchy(indent + "  ", out);
    }
}
