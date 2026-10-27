package tmps.portfolio.domain.factory.regional;

import tmps.portfolio.domain.models.CryptoAsset;
import tmps.portfolio.domain.models.P2PLoanAsset;
import tmps.portfolio.domain.models.StockAsset;

import java.math.BigDecimal;

/**
 * Concrete Factory 1 for US Jurisdiction (USD, SEC/FINRA conventions, NYSE/NASDAQ).
 */
public class USMarketFactory implements FinancialMarketFactory {

    @Override
    public String getJurisdiction() {
        return "United States (SEC/FINRA)";
    }

    @Override
    public String getMarketCurrency() {
        return "USD";
    }

    @Override
    public StockAsset createBenchmarkEquity(String id, double quantity) {
        // S&P 500 Index constituent (e.g. Apple on NASDAQ in USD)
        return new StockAsset(
                id,
                "AAPL",
                "Apple Inc.",
                "NASDAQ",
                BigDecimal.valueOf(quantity),
                new BigDecimal("185.50"),
                new BigDecimal("0.55"),
                "USD"
        );
    }

    @Override
    public P2PLoanAsset createConsumerCredit(String id, String borrowerRef, double principal) {
        // US Prosper/LendingClub consumer loan in USD
        return new P2PLoanAsset(
                id,
                borrowerRef,
                "LendingClub-US",
                BigDecimal.valueOf(principal),
                new BigDecimal("11.50"),
                24,
                new BigDecimal("2.20"),
                "USD"
        );
    }

    @Override
    public CryptoAsset createCompliantCrypto(String id, double amount) {
        // US regulated spot crypto in USD
        return new CryptoAsset(
                id,
                "BTC",
                "Bitcoin",
                BigDecimal.valueOf(amount),
                new BigDecimal("67500.00"),
                new BigDecimal("15.00"),
                "USD"
        );
    }
}
