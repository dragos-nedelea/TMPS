package tmps.portfolio.decorator;

import tmps.portfolio.composite.PortfolioComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * [DECORATOR - CONCRETE DECORATOR]
 * Dynamically applies annual asset management / fund custody fees.
 */
public class ManagementFeeDecorator extends ValuationDecorator {

    private final BigDecimal feePercentage;

    public ManagementFeeDecorator(PortfolioComponent decoratedComponent, BigDecimal feePercentage) {
        super(decoratedComponent);
        if (feePercentage == null || feePercentage.compareTo(BigDecimal.ZERO) < 0 || feePercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Management fee percentage must be between 0% and 100%: " + feePercentage);
        }
        this.feePercentage = feePercentage;
    }

    public BigDecimal getFeePercentage() {
        return feePercentage;
    }

    public BigDecimal calculateFeeAmount() {
        BigDecimal base = decoratedComponent.calculateFairValue();
        return base.multiply(feePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateFairValue() {
        BigDecimal base = decoratedComponent.calculateFairValue();
        BigDecimal fee = calculateFeeAmount();
        return base.subtract(fee).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void printHierarchy(String indent, StringBuilder out) {
        out.append(indent)
                .append("💼 [DECORATOR: Management Fee ")
                .append(feePercentage)
                .append("%] Pre-Fee: ")
                .append(decoratedComponent.calculateFairValue())
                .append(" | Fee Deducted: -")
                .append(calculateFeeAmount())
                .append(" | Post-Fee: ")
                .append(calculateFairValue())
                .append("\n");

        decoratedComponent.printHierarchy(indent + "  ", out);
    }
}
