package tmps.portfolio.domain.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Concrete representation of a publicly traded corporate equity holding.
 */
public class StockAsset implements Asset {

    private final String id;
    private final String ticker;
    private final String companyName;
    private final String exchange;
    private final BigDecimal shareCount;
    private final BigDecimal currentMarketPrice;
    private final BigDecimal dividendYieldPercent;
    private final String currency;

    public StockAsset(
            String id,
            String ticker,
            String companyName,
            String exchange,
            BigDecimal shareCount,
            BigDecimal currentMarketPrice,
            BigDecimal dividendYieldPercent,
            String currency
    ) {
        this.id = Objects.requireNonNull(id, "Asset ID cannot be null");
        this.ticker = Objects.requireNonNull(ticker, "Ticker cannot be null").toUpperCase().trim();
        this.companyName = Objects.requireNonNull(companyName, "Company name cannot be null");
        this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
        this.shareCount = validatePositive(shareCount, "Share count");
        this.currentMarketPrice = validatePositive(currentMarketPrice, "Market price");
        this.dividendYieldPercent = Objects.requireNonNullElse(dividendYieldPercent, BigDecimal.ZERO);
        this.currency = Objects.requireNonNullElse(currency, "USD").toUpperCase().trim();
    }

    private static BigDecimal validatePositive(BigDecimal value, String name) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must be non-negative.");
        }
        return value;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return companyName + " (" + ticker + ") [" + exchange + "]";
    }

    @Override
    public AssetType getType() {
        return AssetType.PUBLIC_EQUITY;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    public String getTicker() {
        return ticker;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getExchange() {
        return exchange;
    }

    public BigDecimal getShareCount() {
        return shareCount;
    }

    public BigDecimal getCurrentMarketPrice() {
        return currentMarketPrice;
    }

    public BigDecimal getDividendYieldPercent() {
        return dividendYieldPercent;
    }

    @Override
    public BigDecimal calculateFairValue() {
        BigDecimal base = shareCount.multiply(currentMarketPrice);
        BigDecimal dividendBonus = base.multiply(dividendYieldPercent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return base.add(dividendBonus).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Prototype Pattern Implementation:
     * Produces a deep copy of this stock asset.
     */
    @Override
    public StockAsset cloneAsset() {
        return new StockAsset(
                this.id,
                this.ticker,
                this.companyName,
                this.exchange,
                this.shareCount,
                this.currentMarketPrice,
                this.dividendYieldPercent,
                this.currency
        );
    }

    /**
     * Customized prototype clone with new ID and share count.
     */
    public StockAsset cloneWithHolding(String newId, BigDecimal newShareCount) {
        return new StockAsset(
                newId,
                this.ticker,
                this.companyName,
                this.exchange,
                newShareCount,
                this.currentMarketPrice,
                this.dividendYieldPercent,
                this.currency
        );
    }

    @Override
    public String toString() {
        return String.format("StockAsset[%s | %s (%s) | %s shares @ %s %s | Value: %s %s]",
                id, companyName, ticker, shareCount, currentMarketPrice, currency, calculateFairValue(), currency);
    }
}
