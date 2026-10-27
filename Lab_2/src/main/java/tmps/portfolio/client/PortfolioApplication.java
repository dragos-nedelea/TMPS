package tmps.portfolio.client;

import tmps.portfolio.composite.AssetLeaf;
import tmps.portfolio.composite.PortfolioComponent;
import tmps.portfolio.composite.PortfolioComposite;
import tmps.portfolio.decorator.CurrencyHedgingDecorator;
import tmps.portfolio.decorator.ManagementFeeDecorator;
import tmps.portfolio.decorator.WithholdingTaxDecorator;
import tmps.portfolio.facade.InvestmentEngineFacade;
import tmps.portfolio.flyweight.ExchangeFlyweightFactory;
import tmps.portfolio.flyweight.ExchangeMetadata;
import tmps.portfolio.proxy.CachedAuditedMarketDataProxy;

import java.math.BigDecimal;
import java.util.List;

/**
 * [SINGLE CLIENT FOR THE SYSTEM]
 * Interactive client demonstrator showcasing the seamless integration
 * of all 7 Structural Design Patterns in the Institutional Investment Engine.
 */
public class PortfolioApplication {

    public void runDemo() {
        printBanner("TMPS LAB 2: STRUCTURAL DESIGN PATTERNS IN INSTITUTIONAL PORTFOLIO MANAGEMENT");
        System.out.println("Course: Software Architecture and Design Patterns (TMPS)");
        System.out.println("Institution: Technical University of Moldova (UTM)");
        System.out.println("Author: Daniel Nedelea\n");

        // Initialize the Facade (All complex wiring, adapters, and proxies are buried inside)
        InvestmentEngineFacade facade = new InvestmentEngineFacade();
        String currentAnalyst = "SeniorAnalyst_DNedelea";

        // =========================================================================
        // 1. FACADE PATTERN DEMO
        // =========================================================================
        printSection("1. FACADE PATTERN: High-Level Portfolio & Asset Orchestration");
        System.out.println("• The client interacts solely with InvestmentEngineFacade.");
        System.out.println("• Factory instantiation, adapter transformations, and proxy caching are hidden.\n");

        // =========================================================================
        // 2. ADAPTER PATTERN DEMO
        // =========================================================================
        printSection("2. ADAPTER PATTERN: Legacy CSV Price Feed -> Modern PriceFeedProvider");
        System.out.println("• Fetching live quote for Apple (AAPL) and Bitcoin (BTC):");
        BigDecimal aaplPrice = facade.fetchMarketPrice("AAPL", currentAnalyst);
        BigDecimal btcPrice = facade.fetchMarketPrice("BTC", currentAnalyst);
        System.out.println("  ✓ AAPL Quote resolved via Adapter : $" + aaplPrice);
        System.out.println("  ✓ BTC  Quote resolved via Adapter : $" + btcPrice + "\n");

        // =========================================================================
        // 3. FLYWEIGHT PATTERN DEMO
        // =========================================================================
        printSection("3. FLYWEIGHT PATTERN: Shared Immutable Market Exchange Metadata");
        AssetLeaf asset1 = facade.createAssetPosition("POS-1", "AAPL", "Apple Inc.", 100, "XNAS", currentAnalyst);
        AssetLeaf asset2 = facade.createAssetPosition("POS-2", "MSFT", "Microsoft Corp.", 80, "XNAS", currentAnalyst);

        System.out.println("• Position 1 Exchange: " + asset1.getExchange());
        System.out.println("• Position 2 Exchange: " + asset2.getExchange());
        System.out.println("• Verifying Reference Identity (asset1.getExchange() == asset2.getExchange()): "
                + (asset1.getExchange() == asset2.getExchange()));
        System.out.println("• Total Exchange Flyweights in Memory Pool: " + ExchangeFlyweightFactory.getPoolSize() + "\n");

        // =========================================================================
        // 4. PROXY PATTERN DEMO
        // =========================================================================
        printSection("4. PROXY PATTERN: In-Memory Caching & Compliance Audit Trail");
        System.out.println("• Querying AAPL price again to test proxy cache hit...");
        long tStart = System.nanoTime();
        BigDecimal cachedAapl = facade.fetchMarketPrice("AAPL", currentAnalyst);
        long durationMicros = (System.nanoTime() - tStart) / 1000;

        CachedAuditedMarketDataProxy proxy = facade.getMarketDataProxy();
        System.out.println("  ✓ Price resolved: $" + cachedAapl + " (Latency: " + durationMicros + " µs)");
        System.out.println("  ✓ Total Cache Hits registered by Proxy: " + proxy.getCacheHitCount());

        System.out.println("\n• Institutional Regulatory Audit Trail recorded by Proxy:");
        List<CachedAuditedMarketDataProxy.AuditLogEntry> auditTrail = proxy.getAuditTrail();
        for (CachedAuditedMarketDataProxy.AuditLogEntry entry : auditTrail) {
            System.out.printf("  [AUDIT] %s | User: %s | Sym: %s | Price: $%s | Source: %s%n",
                    entry.timestamp().toString().substring(11, 19),
                    entry.requester(),
                    entry.symbol(),
                    entry.price(),
                    entry.resolutionSource());
        }
        System.out.println();

        // =========================================================================
        // 5. COMPOSITE PATTERN DEMO
        // =========================================================================
        printSection("5. COMPOSITE PATTERN: Hierarchical Fund-of-Funds Asset Structure");
        PortfolioComposite globalMasterFund = facade.buildDemoGlobalFund(currentAnalyst);

        System.out.println("• Master Fund ID         : " + globalMasterFund.getId());
        System.out.println("• Master Fund Name       : " + globalMasterFund.getName());
        System.out.println("• Total Leaf Assets Held : " + globalMasterFund.getAssetCount() + " positions");
        System.out.println("• Total Gross Fair Value : $" + globalMasterFund.calculateFairValue() + "\n");

        System.out.println("• Tree Hierarchy Rendering:");
        StringBuilder treeBuilder = new StringBuilder();
        globalMasterFund.printHierarchy("  ", treeBuilder);
        System.out.print(treeBuilder);

        // =========================================================================
        // 6. DECORATOR PATTERN DEMO
        // =========================================================================
        printSection("6. DECORATOR PATTERN: Dynamic Fee, Tax & FX Hedging Adjustments");
        System.out.println("• Wrapping Master Fund in chained decorators without altering underlying composite:");
        System.out.println("  1. ManagementFeeDecorator (1.25% annual management & custody fee)");
        System.out.println("  2. WithholdingTaxDecorator (15.00% capital gains withholding tax)");
        System.out.println("  3. CurrencyHedgingDecorator (0.40% FX risk hedge premium into EUR)");

        PortfolioComponent feeAdjusted = new ManagementFeeDecorator(globalMasterFund, new BigDecimal("1.25"));
        PortfolioComponent taxAdjusted = new WithholdingTaxDecorator(feeAdjusted, new BigDecimal("15.00"));
        PortfolioComponent fullyDecorated = new CurrencyHedgingDecorator(taxAdjusted, new BigDecimal("0.40"), "EUR");

        System.out.println("\n  ► Gross Valuation   : $" + globalMasterFund.calculateFairValue());
        System.out.println("  ► Post-Fee Valuation: $" + feeAdjusted.calculateFairValue());
        System.out.println("  ► Post-Tax Valuation: $" + taxAdjusted.calculateFairValue());
        System.out.println("  ► Net Hedged Value  : $" + fullyDecorated.calculateFairValue() + "\n");

        System.out.println("• Decorated Hierarchy Breakdown:");
        StringBuilder decBuilder = new StringBuilder();
        fullyDecorated.printHierarchy("  ", decBuilder);
        System.out.print(decBuilder);

        // =========================================================================
        // 7. BRIDGE PATTERN DEMO
        // =========================================================================
        printSection("7. BRIDGE PATTERN: Multi-Channel Report Abstractions & Renderers");
        System.out.println("• Channel 1: Executive Summary Report (Console Tabular Format)");
        String consoleExecutive = facade.generateReport(fullyDecorated, false, false);
        System.out.print(consoleExecutive);

        System.out.println("• Channel 2: Detailed Audit Report (Structured JSON Format for Microservices)");
        String jsonAudit = facade.generateReport(fullyDecorated, true, true);
        System.out.println(jsonAudit);

        printBanner("ALL 7 STRUCTURAL DESIGN PATTERNS SUCCESSFULLY EXECUTED");
    }

    private void printBanner(String title) {
        String line = "=".repeat(92);
        System.out.println("\n" + line);
        System.out.println("  " + title);
        System.out.println(line);
    }

    private void printSection(String title) {
        String line = "-".repeat(92);
        System.out.println(line);
        System.out.println(title);
        System.out.println(line);
    }
}
