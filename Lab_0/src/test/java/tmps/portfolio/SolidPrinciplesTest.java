package tmps.portfolio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tmps.portfolio.domain.Asset;
import tmps.portfolio.domain.CryptoAsset;
import tmps.portfolio.domain.P2PLoanAsset;
import tmps.portfolio.domain.PortfolioReport;
import tmps.portfolio.domain.StockAsset;
import tmps.portfolio.domain.ValuationResult;
import tmps.portfolio.export.ReportExporter;
import tmps.portfolio.repository.AssetRepository;
import tmps.portfolio.repository.InMemoryAssetRepository;
import tmps.portfolio.service.PortfolioService;
import tmps.portfolio.strategy.CryptoValuationStrategy;
import tmps.portfolio.strategy.DefaultValuationStrategyRegistry;
import tmps.portfolio.strategy.P2PLoanValuationStrategy;
import tmps.portfolio.strategy.StockValuationStrategy;
import tmps.portfolio.strategy.ValuationStrategy;
import tmps.portfolio.strategy.ValuationStrategyProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SOLID Principles Academic Test Suite")
class SolidPrinciplesTest {

    @Test
    @DisplayName("[SRP] Domain models strictly validate invariants and do not calculate valuations")
    void testSingleResponsibilityDomainInvariants() {
        // Stock invariant: shares must be positive
        assertThrows(IllegalArgumentException.class, () ->
                new StockAsset("S1", "AAPL", "Apple", -5, new BigDecimal("150"), BigDecimal.ZERO)
        );

        // P2P Loan invariant: default risk cannot exceed 100%
        assertThrows(IllegalArgumentException.class, () ->
                new P2PLoanAsset("L1", "B1", new BigDecimal("1000"), new BigDecimal("10"), 12, new BigDecimal("105"))
        );

        // Stock valid creation
        StockAsset stock = new StockAsset("S1", "AAPL", "Apple", 10, new BigDecimal("100.00"), new BigDecimal("5.00"));
        assertEquals("Apple (AAPL)", stock.name());

        // Stock domain record does not calculate value itself; separate strategy does
        StockValuationStrategy strategy = new StockValuationStrategy();
        assertTrue(strategy.supports(stock));

        ValuationResult result = strategy.evaluate(stock);
        // 10 * 100 = 1000 + 5% dividend = 1050.00
        assertEquals(new BigDecimal("1050.00"), result.fairValue());
    }

    @Test
    @DisplayName("[OCP] New asset and valuation strategy can be added without modifying existing service code")
    void testOpenClosedPrincipleExtensibility() {
        AssetRepository repo = new InMemoryAssetRepository();
        DefaultValuationStrategyRegistry registry = new DefaultValuationStrategyRegistry();

        // Register initial strategies
        registry.register(new StockValuationStrategy());
        registry.register(new P2PLoanValuationStrategy());

        // Dummy test exporter
        ReportExporter testExporter = report -> {};

        PortfolioService service = new PortfolioService(repo, registry, testExporter);

        // Populate stock asset
        repo.save(new StockAsset("S1", "MSFT", "Microsoft", 10, new BigDecimal("100.00"), BigDecimal.ZERO));

        PortfolioReport report1 = service.evaluatePortfolio("P1", "Investor 1");
        assertEquals(1, report1.items().size());
        assertEquals(new BigDecimal("1000.00"), report1.totalValuation());

        // EXTENSION: Introduce a brand new strategy (CryptoValuationStrategy) and asset (CryptoAsset)
        // Notice: PortfolioService and existing strategies are UNTOUCHED
        registry.register(new CryptoValuationStrategy());
        repo.save(new CryptoAsset("C1", "BTC", "Bitcoin", new BigDecimal("2.0"), new BigDecimal("50000.00"), new BigDecimal("100.00")));

        PortfolioReport report2 = service.evaluatePortfolio("P1", "Investor 1");
        assertEquals(2, report2.items().size());
        // 1000 (stock) + (2 * 50000 - 100) = 1000 + 99900 = 100900.00
        assertEquals(new BigDecimal("100900.00"), report2.totalValuation());
    }

    @Test
    @DisplayName("[DIP] High-level service depends solely on abstractions, allowing interchangeable dependencies")
    void testDependencyInversionPrinciple() {
        // We create custom stubs for the 3 abstractions to prove PortfolioService has NO concrete dependencies

        // 1. Stub AssetRepository
        AssetRepository stubRepo = new AssetRepository() {
            private final List<Asset> list = List.of(
                    new StockAsset("S1", "TEST", "Test Corp", 1, new BigDecimal("50.00"), BigDecimal.ZERO)
            );
            @Override public void save(Asset asset) {}
            @Override public java.util.Optional<Asset> findById(String id) { return java.util.Optional.empty(); }
            @Override public List<Asset> findAll() { return list; }
            @Override public void deleteById(String id) {}
            @Override public int count() { return list.size(); }
        };

        // 2. Stub ValuationStrategyProvider
        ValuationStrategyProvider stubProvider = asset -> new ValuationStrategy() {
            @Override public boolean supports(Asset a) { return true; }
            @Override public ValuationResult evaluate(Asset a) {
                return new ValuationResult(a, new BigDecimal("99.99"), strategyName(), "Stub evaluation");
            }
            @Override public String strategyName() { return "StubStrategy"; }
        };

        // 3. Stub ReportExporter
        AtomicBoolean exportCalled = new AtomicBoolean(false);
        ReportExporter stubExporter = report -> exportCalled.set(true);

        // PortfolioService accepts pure abstractions
        PortfolioService service = new PortfolioService(stubRepo, stubProvider, stubExporter);
        PortfolioReport report = service.evaluateAndExport("STUB-PORTFOLIO", "Test User");

        assertNotNull(report);
        assertEquals(new BigDecimal("99.99"), report.totalValuation());
        assertTrue(exportCalled.get(), "ReportExporter abstraction was successfully invoked");
    }
}
