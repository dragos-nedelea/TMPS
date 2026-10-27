package tmps.portfolio.strategy;

import tmps.portfolio.domain.Asset;
import tmps.portfolio.domain.P2PLoanAsset;
import tmps.portfolio.domain.ValuationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * [SOLID: OCP & SRP]
 * Concrete implementation of ValuationStrategy for P2P Loans.
 * Calculates fair value as:
 * Gross Value = Principal + (Principal * AnnualRate * (RemainingMonths / 12))
 * Risk Discount = Gross Value * (DefaultRiskProbability / 100)
 * Fair Value = Gross Value - Risk Discount
 */
public class P2PLoanValuationStrategy implements ValuationStrategy {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE = new BigDecimal("12");

    @Override
    public boolean supports(Asset asset) {
        return asset instanceof P2PLoanAsset;
    }

    @Override
    public ValuationResult evaluate(Asset asset) {
        if (!(asset instanceof P2PLoanAsset loan)) {
            throw new IllegalArgumentException("P2PLoanValuationStrategy only supports P2PLoanAsset instances");
        }

        BigDecimal principal = loan.principalRemaining();
        BigDecimal annualRateFraction = loan.annualInterestRatePercent().divide(HUNDRED, 6, RoundingMode.HALF_UP);
        BigDecimal yearsFraction = BigDecimal.valueOf(loan.remainingMonths()).divide(TWELVE, 6, RoundingMode.HALF_UP);

        BigDecimal expectedInterest = principal.multiply(annualRateFraction).multiply(yearsFraction);
        BigDecimal grossPayoff = principal.add(expectedInterest);

        BigDecimal riskFraction = loan.defaultRiskProbabilityPercent().divide(HUNDRED, 6, RoundingMode.HALF_UP);
        BigDecimal defaultRiskDiscount = grossPayoff.multiply(riskFraction);

        BigDecimal fairValue = grossPayoff.subtract(defaultRiskDiscount).setScale(2, RoundingMode.HALF_UP);

        String summary = String.format(
                "Principal: $%s, Accrued Interest (%d mo @ %.2f%%): +$%s, Risk Haircut (%.1f%% default prob): -$%s",
                principal.setScale(2, RoundingMode.HALF_UP),
                loan.remainingMonths(),
                loan.annualInterestRatePercent().doubleValue(),
                expectedInterest.setScale(2, RoundingMode.HALF_UP),
                loan.defaultRiskProbabilityPercent().doubleValue(),
                defaultRiskDiscount.setScale(2, RoundingMode.HALF_UP)
        );

        return new ValuationResult(loan, fairValue, strategyName(), summary);
    }

    @Override
    public String strategyName() {
        return "Actuarial Risk-Discounted Cashflow Model";
    }
}
