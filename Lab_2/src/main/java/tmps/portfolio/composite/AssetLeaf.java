package tmps.portfolio.composite;

import tmps.portfolio.flyweight.ExchangeMetadata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * [COMPOSITE - LEAF]
 * Represents an individual financial asset position (e.g. Stock, Crypto, Bond).
 * Holds an extrinsic position quantity and unit price, while referencing the shared
 * intrinsic ExchangeMetadata Flyweight.
 */
public class AssetLeaf implements PortfolioComponent {

    private final String id;
    private final String symbol;
    private final String name;
    private final double quantity;
    private final BigDecimal unitPrice;
    private final ExchangeMetadata exchange; // Flyweight reference

    public AssetLeaf(
            String id,
            String symbol,
            String name,
            double quantity,
            BigDecimal unitPrice,
            ExchangeMetadata exchange
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Asset quantity must be strictly positive: " + quantity);
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.symbol = Objects.requireNonNull(symbol, "symbol cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name + " (" + symbol + ")";
    }

    public String getSymbol() {
        return symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public ExchangeMetadata getExchange() {
        return exchange;
    }

    @Override
    public BigDecimal calculateFairValue() {
        return BigDecimal.valueOf(quantity)
                .multiply(unitPrice)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int getAssetCount() {
        return 1;
    }

    @Override
    public void printHierarchy(String indent, StringBuilder out) {
        out.append(indent)
                .append("├── [LEAF ASSET] ")
                .append(symbol)
                .append(" : ")
                .append(name)
                .append(" | Qty: ")
                .append(String.format("%.2f", quantity))
                .append(" @ ")
                .append(unitPrice)
                .append(" ")
                .append(exchange.getBaseCurrency())
                .append(" = ")
                .append(calculateFairValue())
                .append(" ")
                .append(exchange.getBaseCurrency())
                .append(" (Exchange: ")
                .append(exchange.getMicCode())
                .append(")\n");
    }

    @Override
    public String toString() {
        return "AssetLeaf{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", value=" + calculateFairValue() +
                ", exchange=" + exchange.getMicCode() +
                '}';
    }
}
