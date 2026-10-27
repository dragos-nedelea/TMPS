package tmps.portfolio.proxy;

import java.math.BigDecimal;

/**
 * [PROXY - SUBJECT INTERFACE]
 * Contract for fetching real-time market quotes.
 */
public interface MarketDataService {

    /**
     * Retrieves the market price for an instrument, tracking the identity of the requester.
     */
    BigDecimal fetchLivePrice(String symbol, String requester);
}
