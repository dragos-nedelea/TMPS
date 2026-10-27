package tmps.portfolio.domain.factory;

import tmps.portfolio.domain.models.CryptoAsset;

import java.math.BigDecimal;

/**
 * Concrete Factory Method Creator for Cryptocurrency and digital token assets.
 */
public class CryptoAssetFactory extends AssetFactory<CryptoAsset> {

    private final String defaultCurrency;

    public CryptoAssetFactory(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public CryptoAssetFactory() {
        this("USD");
    }

    @Override
    public CryptoAsset createAsset(String id, String symbol, double coinAmount, double spotPrice) {
        String network = deriveNetwork(symbol);
        BigDecimal estimatedGas = symbol.equalsIgnoreCase("BTC") ? new BigDecimal("12.50") :
                (symbol.equalsIgnoreCase("ETH") ? new BigDecimal("5.80") : new BigDecimal("0.50"));

        return new CryptoAsset(
                id,
                symbol,
                network,
                BigDecimal.valueOf(coinAmount),
                BigDecimal.valueOf(spotPrice),
                estimatedGas,
                defaultCurrency
        );
    }

    private String deriveNetwork(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "BTC" -> "Bitcoin";
            case "ETH" -> "Ethereum";
            case "SOL" -> "Solana";
            default -> symbol.toUpperCase() + "-Net";
        };
    }
}
