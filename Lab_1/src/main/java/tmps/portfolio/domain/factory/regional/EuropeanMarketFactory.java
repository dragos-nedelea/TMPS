package tmps.portfolio.domain.factory.regional;

import tmps.portfolio.domain.models.CryptoAsset;
import tmps.portfolio.domain.models.P2PLoanAsset;
import tmps.portfolio.domain.models.StockAsset;

import java.math.BigDecimal;

/**
 * Concrete Factory 2 for European Union Jurisdiction (EUR, MiFID II / MiCA conventions, Euronext/XETRA).
 */
public class EuropeanMarketFactory implements FinancialMarketFactory {

    @Override
    public String getJurisdiction() {
        return "European Union (ESMA/MiFID II)";
    }

    @Override
    public String getMarketCurrency() {
        return "EUR";
    }

    @Override
    public StockAsset createBenchmarkEquity(String id, double quantity) {
        // Euro Stoxx 50 constituent (e.g. ASML Holding on Euronext in EUR)
        return new StockAsset(
                id,
                "ASML",
                "ASML Holding N.V.",
                "Euronext Amsterdam",
                BigDecimal.valueOf(quantity),
                new BigDecimal("680.00"),
                new BigDecimal("1.40"),
                "EUR"
        );
    }

    @Override
    public P2PLoanAsset createConsumerCredit(String id, String borrowerRef, double principal) {
        // EU Mintos consumer loan in EUR
        return new P2PLoanAsset(
                id,
                borrowerRef,
                "Mintos-EU",
                BigDecimal.valueOf(principal),
                new BigDecimal("9.25"),
                18,
                new BigDecimal("1.80"),
                "EUR"
        );
    }

    @Override
    public CryptoAsset createCompliantCrypto(String id, double amount) {
        // MiCA-regulated Ethereum asset in EUR
        return new CryptoAsset(
                id,
                "ETH",
                "Ethereum",
                BigDecimal.valueOf(amount),
                new BigDecimal("3180.00"),
                new BigDecimal("5.50"),
                "EUR"
        );
    }
}
