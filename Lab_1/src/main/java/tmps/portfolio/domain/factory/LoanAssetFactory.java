package tmps.portfolio.domain.factory;

import tmps.portfolio.domain.models.P2PLoanAsset;

import java.math.BigDecimal;

/**
 * Concrete Factory Method Creator for P2P Debt Loans.
 */
public class LoanAssetFactory extends AssetFactory<P2PLoanAsset> {

    private final String platformName;
    private final String defaultCurrency;

    public LoanAssetFactory(String platformName, String defaultCurrency) {
        this.platformName = platformName;
        this.defaultCurrency = defaultCurrency;
    }

    public LoanAssetFactory() {
        this("Mintos", "USD");
    }

    @Override
    public P2PLoanAsset createAsset(String id, String borrowerRef, double principal, double interestRatePercent) {
        // Standard default parameters: 12 months term, 2.5% actuarial default probability
        return new P2PLoanAsset(
                id,
                borrowerRef,
                platformName,
                BigDecimal.valueOf(principal),
                BigDecimal.valueOf(interestRatePercent),
                12,
                new BigDecimal("2.50"),
                defaultCurrency
        );
    }
}
