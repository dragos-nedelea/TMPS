package tmps.portfolio.client;

import tmps.portfolio.domain.builder.PortfolioBuilder;
import tmps.portfolio.domain.factory.AssetFactory;
import tmps.portfolio.domain.factory.CryptoAssetFactory;
import tmps.portfolio.domain.factory.LoanAssetFactory;
import tmps.portfolio.domain.factory.StockAssetFactory;
import tmps.portfolio.domain.factory.regional.EuropeanMarketFactory;
import tmps.portfolio.domain.factory.regional.FinancialMarketFactory;
import tmps.portfolio.domain.factory.regional.USMarketFactory;
import tmps.portfolio.domain.models.Asset;
import tmps.portfolio.domain.models.CryptoAsset;
import tmps.portfolio.domain.models.P2PLoanAsset;
import tmps.portfolio.domain.models.Portfolio;
import tmps.portfolio.domain.models.StockAsset;
import tmps.portfolio.domain.pool.ValuationWorker;
import tmps.portfolio.domain.pool.ValuationWorkerPool;
import tmps.portfolio.domain.prototype.AssetPrototypeRegistry;
import tmps.portfolio.domain.singleton.MarketDataFeed;

import java.math.BigDecimal;

/**
 * Client demonstrator that showcases all Creational Design Patterns in action.
 */
public class PortfolioApplication {

    public void runDemo() throws Exception {
        printHeader("TMPS LAB 1: CREATIONAL DESIGN PATTERNS DEMONSTRATION");
        System.out.println("Domain: Institutional Investment Portfolio & Financial Asset Management Engine");
        System.out.println("Author: Daniel Nedelea | Technical University of Moldova (UTM)\n");

        // -----------------------------------------------------------------------------------------
        // 1. SINGLETON PATTERN
        // -----------------------------------------------------------------------------------------
        printSection("1. SINGLETON PATTERN: Global Market Data & FX Feed");
        MarketDataFeed feed1 = MarketDataFeed.getInstance();
        MarketDataFeed feed2 = MarketDataFeed.getInstance();

        System.out.println("• Checking reference identity (feed1 == feed2): " + (feed1 == feed2));
        System.out.println("• Thread-safe Bill Pugh Singleton memory hash: " + System.identityHashCode(feed1));
        System.out.println("• Benchmark Treasury Risk-Free Rate: " + feed1.getRiskFreeAnnualRate().multiply(BigDecimal.valueOf(100)) + "%");
        System.out.println("• S&P 500 Current Index Level: " + feed1.getMarketIndexPoints("SP500"));

        BigDecimal eurAmount = new BigDecimal("10000.00");
        BigDecimal convertedToUsd = feed1.convertCurrency(eurAmount, "EUR", "USD");
        System.out.printf("• FX Conversion Test: %s EUR = %s USD (FX rate: %s)%n%n",
                eurAmount, convertedToUsd, feed1.getExchangeRateToUsd("EUR"));

        // -----------------------------------------------------------------------------------------
        // 2. FACTORY METHOD PATTERN
        // -----------------------------------------------------------------------------------------
        printSection("2. FACTORY METHOD PATTERN: Polymorphic Asset Creation");
        AssetFactory<StockAsset> stockFactory = new StockAssetFactory("NASDAQ", "USD");
        AssetFactory<CryptoAsset> cryptoFactory = new CryptoAssetFactory("USD");
        AssetFactory<P2PLoanAsset> loanFactory = new LoanAssetFactory("Mintos", "USD");

        StockAsset appleStock = stockFactory.issueAsset("STK-01", "AAPL", 50.0, 185.50);
        CryptoAsset bitcoinToken = cryptoFactory.issueAsset("CRP-01", "BTC", 1.25, 67500.00);
        P2PLoanAsset consumerLoan = loanFactory.issueAsset("LN-01", "BORR-7721", 5000.0, 11.5);
        System.out.println();

        // -----------------------------------------------------------------------------------------
        // 3. ABSTRACT FACTORY PATTERN
        // -----------------------------------------------------------------------------------------
        printSection("3. ABSTRACT FACTORY PATTERN: Regional Market Product Suites");
        demonstrateRegionalMarket(new USMarketFactory());
        System.out.println();
        demonstrateRegionalMarket(new EuropeanMarketFactory());
        System.out.println();

        // -----------------------------------------------------------------------------------------
        // 4. PROTOTYPE PATTERN
        // -----------------------------------------------------------------------------------------
        printSection("4. PROTOTYPE PATTERN: Cloning Standard Asset Benchmarks");
        AssetPrototypeRegistry prototypeRegistry = new AssetPrototypeRegistry();

        // Clone prototype and customize holding
        StockAsset customizedStock = prototypeRegistry.cloneStock("SP500_MSFT", "STK-CUSTOM-01", 75.0);
        CryptoAsset customizedCrypto = prototypeRegistry.cloneCrypto("CRYPTO_BTC_COLD_STORAGE", "CRP-CUSTOM-01", 3.50);
        P2PLoanAsset customizedLoan = prototypeRegistry.cloneLoan("P2P_AAA_TIER", "LN-CUSTOM-01", "BORR-AAA-992", 15000.0);

        System.out.println("• Cloned Stock Asset:  " + customizedStock);
        System.out.println("• Cloned Crypto Asset: " + customizedCrypto);
        System.out.println("• Cloned P2P Debt:     " + customizedLoan);
        System.out.println();

        // -----------------------------------------------------------------------------------------
        // 5. BUILDER PATTERN
        // -----------------------------------------------------------------------------------------
        printSection("5. BUILDER PATTERN: Assembling Complex Portfolio");
        Portfolio portfolio = new PortfolioBuilder()
                .withId("PORTFOLIO-ALPHA")
                .withInvestor("Daniel Nedelea", "daniel.nedelea@isa.utm.md")
                .withRiskProfile(Portfolio.RiskProfile.GROWTH)
                .withBaseCurrency("USD")
                .withCashBalance(new BigDecimal("25000.00"))
                .addAsset(appleStock)
                .addAsset(bitcoinToken)
                .addAsset(consumerLoan)
                .addAsset(customizedStock)
                .addAsset(customizedCrypto)
                .addAsset(customizedLoan)
                .addTag("Tech-Focus")
                .addTag("High-Yield")
                .addTag("Diversified")
                .build();

        System.out.println("• Successfully assembled immutable Portfolio:");
        System.out.println("  " + portfolio);
        System.out.println("• Portfolio Tags: " + portfolio.getTags());
        System.out.println("• Cash Reserve: $" + portfolio.getCashBalance());
        System.out.println("• Total Asset Appraised Value: $" + portfolio.calculateAssetValuation());
        System.out.println("• Total Consolidated Net Worth: $" + portfolio.calculateNetWorth());
        System.out.println();

        // -----------------------------------------------------------------------------------------
        // 6. OBJECT POOL PATTERN
        // -----------------------------------------------------------------------------------------
        printSection("6. OBJECT POOL PATTERN: High-Throughput Valuation Workers");
        ValuationWorkerPool pool = new ValuationWorkerPool(2);
        System.out.println("• Pool initialized with capacity: " + pool.getCapacity());
        System.out.println("• Available workers: " + pool.getAvailableCount());

        // Check out worker 1
        ValuationWorker worker1 = pool.acquire(500);
        System.out.println("• Checked out Worker #" + worker1.getWorkerId() + " (In-use: " + pool.getInUseCount() + ")");

        // Check out worker 2
        ValuationWorker worker2 = pool.acquire(500);
        System.out.println("• Checked out Worker #" + worker2.getWorkerId() + " (In-use: " + pool.getInUseCount() + ")");

        // Worker 1 processes portfolio analysis
        ValuationWorker.ValuationReport report = worker1.executeValuation(portfolio);
        System.out.printf("  [Worker #%d Result] Evaluated %d assets for %s: Total Net Worth: $%s (Equity: %.1f%%, Debt: %.1f%%, Crypto: %.1f%%)%n",
                report.workerId(), report.assetCount(), report.investor(), report.netWorth(),
                report.equityPct(), report.debtPct(), report.cryptoPct());

        // Release worker 1 back to pool
        pool.release(worker1);
        System.out.println("• Released Worker #" + worker1.getWorkerId() + " back to pool (Available: " + pool.getAvailableCount() + ")");

        // Re-acquire to prove reuse
        ValuationWorker reusedWorker = pool.acquire(500);
        System.out.println("• Re-acquired Worker #" + reusedWorker.getWorkerId() + " (Lifetime jobs processed: " + reusedWorker.getProcessedJobCount() + ")");
        System.out.println("• Proved worker instance reuse: " + (reusedWorker == worker1));

        pool.release(reusedWorker);
        pool.release(worker2);
        System.out.println("• All workers safely returned to pool. Final available count: " + pool.getAvailableCount());

        printHeader("ALL 5 CREATIONAL DESIGN PATTERNS SUCCESSFULLY DEMONSTRATED");
    }

    private void demonstrateRegionalMarket(FinancialMarketFactory factory) {
        System.out.printf("--- Regional Market: %s [Base Currency: %s] ---%n",
                factory.getJurisdiction(), factory.getMarketCurrency());

        Asset equity = factory.createBenchmarkEquity("REG-EQ-01", 100);
        Asset debt = factory.createConsumerCredit("REG-LN-01", "EU-BORR-10", 8000);
        Asset crypto = factory.createCompliantCrypto("REG-CRP-01", 2.0);

        System.out.printf("  1. Equity Instrument:   %s => Value: %s %s%n",
                equity.getName(), equity.calculateFairValue(), equity.getCurrency());
        System.out.printf("  2. Fixed Income Credit: %s => Value: %s %s%n",
                debt.getName(), debt.calculateFairValue(), debt.getCurrency());
        System.out.printf("  3. Digital Asset:       %s => Value: %s %s%n",
                crypto.getName(), crypto.calculateFairValue(), crypto.getCurrency());
    }

    private void printHeader(String title) {
        System.out.println("==========================================================================================");
        System.out.println("  " + title);
        System.out.println("==========================================================================================");
    }

    private void printSection(String sectionTitle) {
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println(">>> " + sectionTitle);
        System.out.println("------------------------------------------------------------------------------------------");
    }
}
