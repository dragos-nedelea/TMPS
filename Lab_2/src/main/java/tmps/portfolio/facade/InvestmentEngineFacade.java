package tmps.portfolio.facade;

import tmps.portfolio.adapter.LegacyCsvPriceFeed;
import tmps.portfolio.adapter.LegacyCsvPriceFeedAdapter;
import tmps.portfolio.adapter.PriceFeedProvider;
import tmps.portfolio.bridge.renderer.ConsoleTabularRenderer;
import tmps.portfolio.bridge.renderer.JsonReportRenderer;
import tmps.portfolio.bridge.renderer.ReportRenderer;
import tmps.portfolio.bridge.report.DetailedAuditReport;
import tmps.portfolio.bridge.report.ExecutiveSummaryReport;
import tmps.portfolio.bridge.report.PortfolioReport;
import tmps.portfolio.composite.AssetLeaf;
import tmps.portfolio.composite.PortfolioComponent;
import tmps.portfolio.composite.PortfolioComposite;
import tmps.portfolio.decorator.CurrencyHedgingDecorator;
import tmps.portfolio.decorator.ManagementFeeDecorator;
import tmps.portfolio.decorator.WithholdingTaxDecorator;
import tmps.portfolio.flyweight.ExchangeFlyweightFactory;
import tmps.portfolio.flyweight.ExchangeMetadata;
import tmps.portfolio.proxy.CachedAuditedMarketDataProxy;
import tmps.portfolio.proxy.MarketDataService;
import tmps.portfolio.proxy.RemoteMarketDataService;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * [FACADE]
 * High-level orchestration facade that simplifies and conceals the inner complexities
 * of the 6 other structural patterns (Composite, Adapter, Decorator, Proxy, Bridge, Flyweight).
 *
 * Provides client applications with a clean, single-point API for managing,
 * pricing, hedging, decorating, and reporting multi-asset institutional portfolios.
 */
public class InvestmentEngineFacade {

    private final MarketDataService marketDataService;
    private final CachedAuditedMarketDataProxy marketDataProxy;

    public InvestmentEngineFacade() {
        // 1. Initialize Adapter: Wrap legacy CSV feed with modern provider interface
        LegacyCsvPriceFeed legacyFeed = new LegacyCsvPriceFeed();
        PriceFeedProvider feedAdapter = new LegacyCsvPriceFeedAdapter(legacyFeed);

        // 2. Initialize Proxy: Wrap remote service with cache & audit logging
        RemoteMarketDataService remoteService = new RemoteMarketDataService(feedAdapter);
        this.marketDataProxy = new CachedAuditedMarketDataProxy(remoteService, 5000L); // 5 sec TTL
        this.marketDataService = this.marketDataProxy;
    }

    /**
     * Creates an empty institutional composite portfolio container.
     */
    public PortfolioComposite createPortfolio(String id, String name, String baseCurrency) {
        return new PortfolioComposite(id, name, baseCurrency);
    }

    /**
     * Creates an individual asset position, fetching its live price through the audited proxy
     * and attaching its exchange flyweight.
     */
    public AssetLeaf createAssetPosition(
            String id,
            String symbol,
            String name,
            double quantity,
            String micCode,
            String requester
    ) {
        // Retrieve or create immutable shared exchange flyweight
        ExchangeMetadata exchange = ExchangeFlyweightFactory.getExchange(micCode);

        // Fetch market price through cached, audited proxy
        BigDecimal livePrice = marketDataService.fetchLivePrice(symbol, requester);

        return new AssetLeaf(id, symbol, name, quantity, livePrice, exchange);
    }

    /**
     * Builds a pre-configured multi-tier Global Institutional Hedge Fund
     * demonstrating the Composite pattern with nested sub-funds.
     */
    public PortfolioComposite buildDemoGlobalFund(String requester) {
        // Master Composite Fund
        PortfolioComposite masterFund = createPortfolio("FUND-GLOBAL-01", "Global Institutional Alpha Hedge Fund", "USD");

        // Sub-Fund 1: North American Tech Growth (Composite)
        PortfolioComposite techFund = createPortfolio("SUB-TECH-US", "US High-Growth Tech Sector Basket", "USD");
        techFund.add(createAssetPosition("AST-AAPL", "AAPL", "Apple Inc.", 250.0, "XNAS", requester));
        techFund.add(createAssetPosition("AST-MSFT", "MSFT", "Microsoft Corporation", 180.0, "XNAS", requester));
        techFund.add(createAssetPosition("AST-GOOGL", "GOOGL", "Alphabet Inc.", 200.0, "XNAS", requester));

        // Sub-Fund 2: European Equities Basket (Composite)
        PortfolioComposite euroFund = createPortfolio("SUB-EURO-EQ", "European Blue-Chip Equity Basket", "EUR");
        euroFund.add(createAssetPosition("AST-LVMH", "AAPL", "LVMH Moët Hennessy (Proxy Rate)", 100.0, "XPAR", requester));

        // Sub-Fund 3: Digital Asset & Crypto Reserve (Composite)
        PortfolioComposite cryptoFund = createPortfolio("SUB-CRYPTO-RES", "Liquid Digital Asset Reserve", "USD");
        cryptoFund.add(createAssetPosition("AST-BTC", "BTC", "Bitcoin Treasury Reserve", 3.5, "BINC", requester));
        cryptoFund.add(createAssetPosition("AST-ETH", "ETH", "Ethereum Staking Basket", 45.0, "BINC", requester));

        // Assemble into Master Fund
        masterFund.add(techFund);
        masterFund.add(euroFund);
        masterFund.add(cryptoFund);

        return masterFund;
    }

    /**
     * Applies standard institutional deductions (annual management fee + capital gains withholding tax)
     * using the Decorator pattern.
     */
    public PortfolioComponent applyFeeAndTaxPillars(
            PortfolioComponent component,
            BigDecimal managementFeePct,
            BigDecimal taxRatePct
    ) {
        PortfolioComponent feeAdjusted = new ManagementFeeDecorator(component, managementFeePct);
        return new WithholdingTaxDecorator(feeAdjusted, taxRatePct);
    }

    /**
     * Applies foreign exchange hedging protection cost using the Decorator pattern.
     */
    public PortfolioComponent applyCurrencyHedging(
            PortfolioComponent component,
            BigDecimal hedgeCostPct,
            String targetCurrency
    ) {
        return new CurrencyHedgingDecorator(component, hedgeCostPct, targetCurrency);
    }

    /**
     * Generates a portfolio report using the Bridge pattern (Report Abstraction + ReportRenderer Implementor).
     */
    public String generateReport(PortfolioComponent component, boolean detailed, boolean asJson) {
        ReportRenderer renderer = asJson ? new JsonReportRenderer() : new ConsoleTabularRenderer();
        PortfolioReport report = detailed ? new DetailedAuditReport(renderer) : new ExecutiveSummaryReport(renderer);
        return report.generate(component);
    }

    /**
     * Queries a live price directly via proxy.
     */
    public BigDecimal fetchMarketPrice(String symbol, String requester) {
        return marketDataService.fetchLivePrice(symbol, requester);
    }

    public CachedAuditedMarketDataProxy getMarketDataProxy() {
        return marketDataProxy;
    }
}
