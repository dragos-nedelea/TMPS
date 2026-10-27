package tmps.portfolio.adapter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * [ADAPTER - CONCRETE ADAPTER]
 * Adapts the incompatible LegacyCsvPriceFeed to fulfill the modern PriceFeedProvider interface.
 * Parses raw legacy semicolon-delimited CSV payloads into typed BigDecimal values.
 */
public class LegacyCsvPriceFeedAdapter implements PriceFeedProvider {

    private final LegacyCsvPriceFeed legacyFeed;

    public LegacyCsvPriceFeedAdapter(LegacyCsvPriceFeed legacyFeed) {
        this.legacyFeed = Objects.requireNonNull(legacyFeed, "legacyFeed cannot be null");
    }

    @Override
    public BigDecimal getLatestPrice(String symbol) {
        String rawRecord = legacyFeed.fetchLegacyCsvQuote(symbol);

        // Parsing logic: "TICKER=AAPL;PRICE=185.50;CURRENCY=USD;STATUS=CONFIRMED"
        String[] tokens = rawRecord.split(";");
        String priceStr = null;
        String status = null;

        for (String token : tokens) {
            String[] pair = token.split("=");
            if (pair.length == 2) {
                String key = pair[0].trim();
                String value = pair[1].trim();

                if ("PRICE".equalsIgnoreCase(key)) {
                    priceStr = value;
                } else if ("STATUS".equalsIgnoreCase(key)) {
                    status = value;
                }
            }
        }

        if (!"CONFIRMED".equalsIgnoreCase(status) || priceStr == null) {
            throw new IllegalArgumentException("Unable to resolve quote for symbol: " + symbol + " (Raw: " + rawRecord + ")");
        }

        try {
            return new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Corrupt numeric value in legacy price quote: " + priceStr, e);
        }
    }

    @Override
    public String getProviderName() {
        return "Legacy CSV Market Feed Adapter v1.2";
    }
}
