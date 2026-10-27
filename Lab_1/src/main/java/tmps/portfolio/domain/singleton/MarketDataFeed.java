package tmps.portfolio.domain.singleton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton Pattern: Authoritative Market Data & FX Feed.
 *
 * Problem Solved:
 * Financial systems require a single source of truth for exchange rates and market benchmarks.
 * Multiple instances could yield conflicting valuations or out-of-sync rate caches.
 *
 * Implementation:
 * Uses the Bill Pugh "Initialization-on-demand Holder Idiom".
 * Provides lazy loading and guaranteed thread-safety by leveraging JVM class-loading guarantees.
 */
public final class MarketDataFeed {

    // Thread-safe map for FX rates relative to USD (1.0)
    private final Map<String, BigDecimal> exchangeRatesToUsd = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> marketIndexPoints = new ConcurrentHashMap<>();
    private volatile BigDecimal riskFreeAnnualRate = new BigDecimal("0.045"); // 4.5% treasury base

    // Private constructor prevents direct external instantiation
    private MarketDataFeed() {
        // Guard against reflection attacks
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("MarketDataFeed instance already exists. Direct instantiation disallowed.");
        }

        // Initialize default FX rates (relative to USD)
        exchangeRatesToUsd.put("USD", BigDecimal.ONE);
        exchangeRatesToUsd.put("EUR", new BigDecimal("1.0850")); // 1 EUR = 1.0850 USD
        exchangeRatesToUsd.put("GBP", new BigDecimal("1.2950")); // 1 GBP = 1.2950 USD
        exchangeRatesToUsd.put("MDL", new BigDecimal("0.0560")); // 1 MDL = 0.0560 USD

        marketIndexPoints.put("SP500", new BigDecimal("5820.00"));
        marketIndexPoints.put("NASDAQ", new BigDecimal("18500.00"));
        marketIndexPoints.put("BTC_DOMINANCE", new BigDecimal("58.4"));
    }

    /**
     * Static inner class responsible for holding the single instance.
     * The JVM guarantees thread-safe lazy class initialization.
     */
    private static class Holder {
        private static final MarketDataFeed INSTANCE = new MarketDataFeed();
    }

    /**
     * Global access point for the Singleton instance.
     */
    public static MarketDataFeed getInstance() {
        return Holder.INSTANCE;
    }

    public void setExchangeRate(String currency, BigDecimal rateToUsd) {
        if (rateToUsd == null || rateToUsd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive.");
        }
        exchangeRatesToUsd.put(currency.toUpperCase(), rateToUsd);
    }

    public BigDecimal getExchangeRateToUsd(String currency) {
        BigDecimal rate = exchangeRatesToUsd.get(currency.toUpperCase());
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency rate: " + currency);
        }
        return rate;
    }

    /**
     * Converts an amount from sourceCurrency to targetCurrency using USD triangulation.
     */
    public BigDecimal convertCurrency(BigDecimal amount, String sourceCurrency, String targetCurrency) {
        if (amount == null) return BigDecimal.ZERO;
        if (sourceCurrency.equalsIgnoreCase(targetCurrency)) return amount;

        BigDecimal sourceRate = getExchangeRateToUsd(sourceCurrency);
        BigDecimal targetRate = getExchangeRateToUsd(targetCurrency);

        // Convert source -> USD
        BigDecimal amountInUsd = amount.multiply(sourceRate);
        // Convert USD -> target
        return amountInUsd.divide(targetRate, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRiskFreeAnnualRate() {
        return riskFreeAnnualRate;
    }

    public void setRiskFreeAnnualRate(BigDecimal rate) {
        this.riskFreeAnnualRate = rate;
    }

    public BigDecimal getMarketIndexPoints(String indexName) {
        return marketIndexPoints.getOrDefault(indexName.toUpperCase(), BigDecimal.ZERO);
    }
}
