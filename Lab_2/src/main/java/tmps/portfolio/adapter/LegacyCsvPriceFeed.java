package tmps.portfolio.adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * [ADAPTER - ADAPTEE]
 * Legacy third-party financial data feed service (e.g. older Bloomberg/Reuters batch service).
 * Its interface is incompatible with our system: it returns raw delimited strings instead
 * of BigDecimal domain objects.
 */
public class LegacyCsvPriceFeed {

    private final Map<String, String> legacyDatabase = new HashMap<>();

    public LegacyCsvPriceFeed() {
        // Seed mock legacy data lines in the legacy format:
        // "TICKER=...;PRICE=...;CURRENCY=...;STATUS=..."
        legacyDatabase.put("AAPL", "TICKER=AAPL;PRICE=185.50;CURRENCY=USD;STATUS=CONFIRMED");
        legacyDatabase.put("MSFT", "TICKER=MSFT;PRICE=420.00;CURRENCY=USD;STATUS=CONFIRMED");
        legacyDatabase.put("GOOGL", "TICKER=GOOGL;PRICE=175.20;CURRENCY=USD;STATUS=CONFIRMED");
        legacyDatabase.put("BTC", "TICKER=BTC;PRICE=67500.00;CURRENCY=USDT;STATUS=CONFIRMED");
        legacyDatabase.put("ETH", "TICKER=ETH;PRICE=3450.00;CURRENCY=USDT;STATUS=CONFIRMED");
        legacyDatabase.put("EURUSD", "TICKER=EURUSD;PRICE=1.0850;CURRENCY=USD;STATUS=CONFIRMED");
    }

    /**
     * Legacy retrieval method returning a raw semicolon-separated record.
     */
    public String fetchLegacyCsvQuote(String instrumentCode) {
        String record = legacyDatabase.get(instrumentCode.toUpperCase());
        if (record == null) {
            return "TICKER=" + instrumentCode + ";PRICE=0.00;CURRENCY=N/A;STATUS=NOT_FOUND";
        }
        return record;
    }

    public void setLegacyPrice(String instrumentCode, String price, String currency) {
        legacyDatabase.put(instrumentCode.toUpperCase(),
                "TICKER=" + instrumentCode.toUpperCase() + ";PRICE=" + price + ";CURRENCY=" + currency + ";STATUS=CONFIRMED");
    }
}
