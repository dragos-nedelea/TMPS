package tmps.portfolio.domain.factory;

import tmps.portfolio.domain.models.StockAsset;

import java.math.BigDecimal;

/**
 * Concrete Factory Method Creator for Stock Assets.
 */
public class StockAssetFactory extends AssetFactory<StockAsset> {

    private final String defaultExchange;
    private final String defaultCurrency;

    public StockAssetFactory(String defaultExchange, String defaultCurrency) {
        this.defaultExchange = defaultExchange;
        this.defaultCurrency = defaultCurrency;
    }

    public StockAssetFactory() {
        this("NASDAQ", "USD");
    }

    @Override
    public StockAsset createAsset(String id, String ticker, double shareCount, double currentPrice) {
        // Automatically default corporate name and dividend estimate based on ticker
        String companyName = deriveCompanyName(ticker);
        BigDecimal dividendEstimate = ticker.equalsIgnoreCase("AAPL") ? new BigDecimal("0.55") :
                (ticker.equalsIgnoreCase("MSFT") ? new BigDecimal("0.75") : new BigDecimal("1.20"));

        return new StockAsset(
                id,
                ticker,
                companyName,
                defaultExchange,
                BigDecimal.valueOf(shareCount),
                BigDecimal.valueOf(currentPrice),
                dividendEstimate,
                defaultCurrency
        );
    }

    private String deriveCompanyName(String ticker) {
        return switch (ticker.toUpperCase()) {
            case "AAPL" -> "Apple Inc.";
            case "MSFT" -> "Microsoft Corporation";
            case "GOOGL" -> "Alphabet Inc.";
            case "AMZN" -> "Amazon.com Inc.";
            case "NVDA" -> "Nvidia Corporation";
            default -> ticker.toUpperCase() + " Corporation";
        };
    }
}
