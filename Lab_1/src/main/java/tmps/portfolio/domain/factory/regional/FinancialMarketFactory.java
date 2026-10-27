package tmps.portfolio.domain.factory.regional;

import tmps.portfolio.domain.models.CryptoAsset;
import tmps.portfolio.domain.models.P2PLoanAsset;
import tmps.portfolio.domain.models.StockAsset;

/**
 * Abstract Factory Pattern: Financial Market Suite Factory.
 *
 * Problem Solved:
 * Financial instruments in different jurisdictions (US vs European Union) have strict regional
 * constraints (currencies, regulatory disclosure, default exchanges).
 * This interface produces coordinated families of related financial assets.
 */
public interface FinancialMarketFactory {

    String getJurisdiction();

    String getMarketCurrency();

    StockAsset createBenchmarkEquity(String id, double quantity);

    P2PLoanAsset createConsumerCredit(String id, String borrowerRef, double principal);

    CryptoAsset createCompliantCrypto(String id, double amount);
}
