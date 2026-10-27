package tmps.portfolio.domain.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Concrete representation of a digital cryptocurrency or token asset.
 */
public class CryptoAsset implements Asset {

    private final String id;
    private final String symbol;
    private final String networkName;
    private final BigDecimal coinAmount;
    private final BigDecimal spotPrice;
    private final BigDecimal networkGasFee;
    private final String currency;

    public CryptoAsset(
            String id,
            String symbol,
            String networkName,
            BigDecimal coinAmount,
            BigDecimal spotPrice,
            BigDecimal networkGasFee,
            String currency
    ) {
        this.id = Objects.requireNonNull(id, "Asset ID cannot be null");
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null").toUpperCase().trim();
        this.networkName = Objects.requireNonNull(networkName, "Network name cannot be null");
        this.coinAmount = validateNonNegative(coinAmount, "Coin amount");
        this.spotPrice = validateNonNegative(spotPrice, "Spot price");
        this.networkGasFee = Objects.requireNonNullElse(networkGasFee, BigDecimal.ZERO);
        this.currency = Objects.requireNonNullElse(currency, "USD").toUpperCase().trim();
    }

    private static BigDecimal validateNonNegative(BigDecimal val, String name) {
        if (val == null || val.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must be non-negative.");
        }
        return val;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return symbol + " (" + networkName + " Network)";
    }

    @Override
    public AssetType getType() {
        return AssetType.CRYPTO_TOKEN;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getNetworkName() {
        return networkName;
    }

    public BigDecimal getCoinAmount() {
        return coinAmount;
    }

    public BigDecimal getSpotPrice() {
        return spotPrice;
    }

    public BigDecimal getNetworkGasFee() {
        return networkGasFee;
    }

    @Override
    public BigDecimal calculateFairValue() {
        BigDecimal gross = coinAmount.multiply(spotPrice);
        BigDecimal net = gross.subtract(networkGasFee);
        return (net.compareTo(BigDecimal.ZERO) > 0 ? net : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Prototype Pattern Implementation:
     * Produces a deep copy of this crypto asset.
     */
    @Override
    public CryptoAsset cloneAsset() {
        return new CryptoAsset(
                this.id,
                this.symbol,
                this.networkName,
                this.coinAmount,
                this.spotPrice,
                this.networkGasFee,
                this.currency
        );
    }

    public CryptoAsset cloneWithHolding(String newId, BigDecimal newCoinAmount) {
        return new CryptoAsset(
                newId,
                this.symbol,
                this.networkName,
                newCoinAmount,
                this.spotPrice,
                this.networkGasFee,
                this.currency
        );
    }

    @Override
    public String toString() {
        return String.format("CryptoAsset[%s | %s on %s | %s coins @ %s %s (gas: %s) | Value: %s %s]",
                id, symbol, networkName, coinAmount, spotPrice, currency, networkGasFee, calculateFairValue(), currency);
    }
}
