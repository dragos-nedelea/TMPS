package tmps.portfolio;

import tmps.portfolio.domain.CryptoAsset;
import tmps.portfolio.domain.P2PLoanAsset;
import tmps.portfolio.domain.StockAsset;
import tmps.portfolio.export.ConsoleReportExporter;
import tmps.portfolio.export.JsonReportExporter;
import tmps.portfolio.export.ReportExporter;
import tmps.portfolio.repository.AssetRepository;
import tmps.portfolio.repository.InMemoryAssetRepository;
import tmps.portfolio.service.PortfolioService;
import tmps.portfolio.strategy.CryptoValuationStrategy;
import tmps.portfolio.strategy.DefaultValuationStrategyRegistry;
import tmps.portfolio.strategy.P2PLoanValuationStrategy;
import tmps.portfolio.strategy.StockValuationStrategy;

import java.math.BigDecimal;

/**
 * Main application entry point acting as the Composition Root.
 *
 * This class demonstrates:
 * 1. Single Responsibility Principle (SRP):
 *    - Domain records store data and validation rules only.
 *    - Valuation math is isolated in ValuationStrategy implementations.
 *    - Exporters format the output.
 *    - PortfolioService orchestrates the workflow.
 *
 * 2. Open/Closed Principle (OCP):
 *    - Adding a new asset (CryptoAsset) and its pricing model (CryptoValuationStrategy)
 *      requires ZERO modifications to existing service or valuation code.
 *
 * 3. Dependency Inversion Principle (DIP):
 *    - High-level PortfolioService depends strictly on abstractions (AssetRepository,
 *      ValuationStrategyProvider, ReportExporter), injected via constructor.
 */
public class Main {

    public static void main(String[] args) {
        printBanner("TMPS LAB 0: INVESTMENT PORTFOLIO VALUATION ENGINE");
        System.out.println("Demonstrating 3 SOLID Principles: SRP, OCP, and DIP");
        System.out.println("No external frameworks used; pure OOP composition root.");

        // =========================================================================
        // STEP 1: COMPOSITION ROOT (DIP - Constructor Injection of Abstractions)
        // =========================================================================
        System.out.println("\n[DIP] Assembling components via Constructor Injection (Composition Root)...");

        // 1. Storage abstraction implementation
        AssetRepository repository = new InMemoryAssetRepository();

        // 2. Strategy Registry abstraction implementation
        DefaultValuationStrategyRegistry strategyRegistry = new DefaultValuationStrategyRegistry();
        strategyRegistry.register(new StockValuationStrategy());
        strategyRegistry.register(new P2PLoanValuationStrategy());

        // 3. Exporter abstraction implementation
        ReportExporter consoleExporter = new ConsoleReportExporter();

        // 4. High-level service: strictly depends on abstractions (AssetRepository,
        //    ValuationStrategyProvider, ReportExporter)
        PortfolioService portfolioService = new PortfolioService(
                repository,
                strategyRegistry,
                consoleExporter
        );

        // =========================================================================
        // STEP 2: SEED SAMPLE ASSETS (SRP: Pure domain records with invariants)
        // =========================================================================
        System.out.println("[SRP] Seeding sample domain assets (Stocks & P2P Loans)...");

        // Stock 1: Apple Inc.
        repository.save(new StockAsset(
                "STK-001",
                "AAPL",
                "Apple Inc.",
                50,
                new BigDecimal("185.50"),
                new BigDecimal("0.55") // 0.55% dividend yield
        ));

        // Stock 2: Microsoft Corporation
        repository.save(new StockAsset(
                "STK-002",
                "MSFT",
                "Microsoft Corp.",
                30,
                new BigDecimal("420.00"),
                new BigDecimal("0.75") // 0.75% dividend yield
        ));

        // P2P Loan 1: PeerBerry Consumer Loan
        repository.save(new P2PLoanAsset(
                "LN-101",
                "BORR-8831",
                new BigDecimal("5000.00"),
                new BigDecimal("11.50"), // 11.5% APR
                18,                      // 18 months remaining
                new BigDecimal("3.0")    // 3.0% default risk
        ));

        // P2P Loan 2: Mintos Real Estate Backed Loan
        repository.save(new P2PLoanAsset(
                "LN-102",
                "BORR-9042",
                new BigDecimal("12000.00"),
                new BigDecimal("9.00"),  // 9.0% APR
                24,                      // 24 months remaining
                new BigDecimal("1.5")    // 1.5% default risk
        ));

        // =========================================================================
        // STEP 3: EXECUTE VALUATION & CONSOLE EXPORT
        // =========================================================================
        System.out.println("\n[RUN 1] Evaluating Portfolio with initial Stock & P2P strategies...");
        portfolioService.evaluateAndExport("PORTFOLIO-ALPHA", "Daniel Nedelea");

        // =========================================================================
        // STEP 4: DEMONSTRATING OCP (Open for extension, Closed for modification)
        // =========================================================================
        printBanner("DEMONSTRATING OCP: EXTENDING WITHOUT MODIFYING");
        System.out.println("Scenario: Adding support for a brand new asset class: CRYPTOCURRENCY.");
        System.out.println("-> Zero changes were made to PortfolioService!");
        System.out.println("-> Zero changes were made to StockValuationStrategy or P2PLoanValuationStrategy!");
        System.out.println("-> We simply register CryptoValuationStrategy into the Strategy Registry.");

        // Register new strategy into the registry at runtime (OCP in action)
        strategyRegistry.register(new CryptoValuationStrategy());

        // Add crypto assets into repository
        repository.save(new CryptoAsset(
                "CRP-201",
                "BTC",
                "Bitcoin",
                new BigDecimal("1.25000000"),
                new BigDecimal("67500.00"),
                new BigDecimal("15.50") // Estimated gas/network fee
        ));

        repository.save(new CryptoAsset(
                "CRP-202",
                "ETH",
                "Ethereum",
                new BigDecimal("8.50000000"),
                new BigDecimal("3450.00"),
                new BigDecimal("6.20")  // Estimated gas/network fee
        ));

        System.out.println("\n[RUN 2] Re-evaluating portfolio after extending with Crypto assets...");
        portfolioService.evaluateAndExport("PORTFOLIO-ALPHA", "Daniel Nedelea");

        // =========================================================================
        // STEP 5: DEMONSTRATING DIP & SRP WITH SWAPPABLE EXPORTER (JSON)
        // =========================================================================
        printBanner("DEMONSTRATING DIP & SRP: SWAPPING OUTPUT DESTINATION");
        System.out.println("Switching from ConsoleReportExporter to JsonReportExporter.");
        System.out.println("PortfolioService easily accommodates a different ReportExporter implementation.");

        PortfolioService jsonReportingService = new PortfolioService(
                repository,
                strategyRegistry,
                new JsonReportExporter()
        );

        System.out.println("[JSON Output]:");
        jsonReportingService.evaluateAndExport("PORTFOLIO-ALPHA", "Daniel Nedelea");

        printBanner("ALL SOLID PRINCIPLES VERIFIED SUCCESSFULLY");
    }

    private static void printBanner(String title) {
        String bar = "=".repeat(90);
        System.out.println("\n" + bar);
        System.out.println("  " + title);
        System.out.println(bar);
    }
}
