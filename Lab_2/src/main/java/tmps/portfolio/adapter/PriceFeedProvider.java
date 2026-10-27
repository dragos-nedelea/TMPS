package tmps.portfolio.adapter;

import java.math.BigDecimal;

/**
 * [ADAPTER - TARGET INTERFACE]
 * Standard internal interface required by the portfolio engine
 * for querying market prices.
 */
public interface PriceFeedProvider {

    /**
     * Retrieves the latest verified fair market price for a given ticker or asset symbol.
     */
    BigDecimal getLatestPrice(String symbol);

    /**
     * Identifier of the price feed source.
     */
    String getProviderName();
}
