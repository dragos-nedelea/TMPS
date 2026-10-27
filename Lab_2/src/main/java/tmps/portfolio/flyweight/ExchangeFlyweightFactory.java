package tmps.portfolio.flyweight;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [FLYWEIGHT FACTORY]
 * Manages the pool of shared immutable ExchangeMetadata flyweights.
 * Ensures that only one instance of an exchange exists in memory.
 */
public final class ExchangeFlyweightFactory {

    private static final Map<String, ExchangeMetadata> POOL = new ConcurrentHashMap<>();

    static {
        // Pre-seed known institutional exchanges
        registerExchange(new ExchangeMetadata("XNYS", "New York Stock Exchange", "United States", "America/New_York", "USD", "SEC"));
        registerExchange(new ExchangeMetadata("XNAS", "NASDAQ Stock Market", "United States", "America/New_York", "USD", "SEC"));
        registerExchange(new ExchangeMetadata("XLON", "London Stock Exchange", "United Kingdom", "Europe/London", "GBP", "FCA"));
        registerExchange(new ExchangeMetadata("XPAR", "Euronext Paris", "France", "Europe/Paris", "EUR", "AMF / ESMA"));
        registerExchange(new ExchangeMetadata("XFRA", "Deutsche Börse Frankfurt", "Germany", "Europe/Berlin", "EUR", "BaFin"));
        registerExchange(new ExchangeMetadata("BINC", "Binance Global Spot", "International", "UTC", "USDT", "VASP Registered"));
    }

    private ExchangeFlyweightFactory() {
        // Utility factory
    }

    public static ExchangeMetadata registerExchange(ExchangeMetadata metadata) {
        return POOL.putIfAbsent(metadata.getMicCode().toUpperCase(), metadata);
    }

    /**
     * Retrieves or creates a shared exchange flyweight.
     */
    public static ExchangeMetadata getExchange(String micCode) {
        String key = micCode.toUpperCase();
        ExchangeMetadata metadata = POOL.get(key);
        if (metadata == null) {
            // Default fallback if unknown
            metadata = POOL.computeIfAbsent(key, k ->
                    new ExchangeMetadata(k, "Generic OTC Market (" + k + ")", "International", "UTC", "USD", "Standard Financial Authority")
            );
        }
        return metadata;
    }

    public static int getPoolSize() {
        return POOL.size();
    }

    public static Map<String, ExchangeMetadata> getRegisteredExchanges() {
        return Collections.unmodifiableMap(POOL);
    }
}
