package tmps.portfolio.domain.prototype;

import tmps.portfolio.domain.models.Asset;
import tmps.portfolio.domain.models.CryptoAsset;
import tmps.portfolio.domain.models.P2PLoanAsset;
import tmps.portfolio.domain.models.StockAsset;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prototype Pattern: Asset Prototype Registry.
 *
 * Problem Solved:
 * Creating complex financial instruments from scratch requires configuring many parameters
 * (exchanges, tickers, dividend expectations, actuarial default tables, network gas fees).
 * The Prototype Registry pre-configures verified baseline benchmark prototypes, allowing clients
 * to clone and customize instances instantly.
 */
public class AssetPrototypeRegistry {

    private final Map<String, Asset> prototypes = new ConcurrentHashMap<>();

    public AssetPrototypeRegistry() {
        loadBenchmarkPrototypes();
    }

    private void loadBenchmarkPrototypes() {
        // Prototype 1: S&P 500 Tech Blue-chip Equity Prototype
        prototypes.put("SP500_TECH_APPLE", new StockAsset(
                "PROTO-AAPL",
                "AAPL",
                "Apple Inc.",
                "NASDAQ",
                BigDecimal.ONE, // Baseline unit
                new BigDecimal("185.50"),
                new BigDecimal("0.55"),
                "USD"
        ));

        // Prototype 2: Conservative S&P 500 Enterprise Software Prototype
        prototypes.put("SP500_MSFT", new StockAsset(
                "PROTO-MSFT",
                "MSFT",
                "Microsoft Corporation",
                "NASDAQ",
                BigDecimal.ONE,
                new BigDecimal("420.00"),
                new BigDecimal("0.75"),
                "USD"
        ));

        // Prototype 3: Vetted Low-Risk P2P Consumer Loan Prototype
        prototypes.put("P2P_AAA_TIER", new P2PLoanAsset(
                "PROTO-P2P-AAA",
                "BORR-BENCHMARK-AAA",
                "Mintos-Prime",
                new BigDecimal("1000.00"),
                new BigDecimal("9.50"),
                12,
                new BigDecimal("1.20"), // 1.2% actuarial default risk
                "USD"
        ));

        // Prototype 4: Blue-chip Digital Asset Prototype
        prototypes.put("CRYPTO_BTC_COLD_STORAGE", new CryptoAsset(
                "PROTO-BTC",
                "BTC",
                "Bitcoin",
                BigDecimal.ONE,
                new BigDecimal("67500.00"),
                new BigDecimal("12.00"),
                "USD"
        ));
    }

    public void registerPrototype(String key, Asset prototype) {
        if (key == null || prototype == null) {
            throw new IllegalArgumentException("Key and prototype cannot be null.");
        }
        prototypes.put(key, prototype);
    }

    /**
     * Retrieves an exact clone of the requested prototype.
     */
    public Asset createClone(String key) {
        Asset proto = prototypes.get(key);
        if (proto == null) {
            throw new IllegalArgumentException("No prototype found for key: " + key);
        }
        return proto.cloneAsset();
    }

    /**
     * Clones a stock prototype and customizes holding quantity and identifier.
     */
    public StockAsset cloneStock(String key, String newId, double newShareCount) {
        Asset proto = prototypes.get(key);
        if (proto instanceof StockAsset stockProto) {
            return stockProto.cloneWithHolding(newId, BigDecimal.valueOf(newShareCount));
        }
        throw new IllegalArgumentException("Key does not point to a StockAsset prototype: " + key);
    }

    /**
     * Clones a crypto prototype and customizes token amount and identifier.
     */
    public CryptoAsset cloneCrypto(String key, String newId, double newCoinAmount) {
        Asset proto = prototypes.get(key);
        if (proto instanceof CryptoAsset cryptoProto) {
            return cryptoProto.cloneWithHolding(newId, BigDecimal.valueOf(newCoinAmount));
        }
        throw new IllegalArgumentException("Key does not point to a CryptoAsset prototype: " + key);
    }

    /**
     * Clones a P2P loan prototype and customizes borrower ID, principal, and identifier.
     */
    public P2PLoanAsset cloneLoan(String key, String newId, String newBorrowerId, double newPrincipal) {
        Asset proto = prototypes.get(key);
        if (proto instanceof P2PLoanAsset loanProto) {
            return loanProto.cloneWithBorrower(newId, newBorrowerId, BigDecimal.valueOf(newPrincipal));
        }
        throw new IllegalArgumentException("Key does not point to a P2PLoanAsset prototype: " + key);
    }
}
