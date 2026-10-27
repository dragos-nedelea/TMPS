package tmps.portfolio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tmps.portfolio.adapter.LegacyCsvPriceFeed;
import tmps.portfolio.adapter.LegacyCsvPriceFeedAdapter;
import tmps.portfolio.adapter.PriceFeedProvider;
import tmps.portfolio.bridge.renderer.ConsoleTabularRenderer;
import tmps.portfolio.bridge.renderer.JsonReportRenderer;
import tmps.portfolio.bridge.report.DetailedAuditReport;
import tmps.portfolio.bridge.report.ExecutiveSummaryReport;
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
import tmps.portfolio.proxy.RemoteMarketDataService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lab 2: Structural Design Patterns Unit Tests")
public class StructuralPatternsTest {

    // =========================================================================
    // 1. ADAPTER PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("1. Adapter Pattern (LegacyCsvPriceFeedAdapter)")
    class AdapterTests {

        private LegacyCsvPriceFeed legacyFeed;
        private PriceFeedProvider adapter;

        @BeforeEach
        void setUp() {
            legacyFeed = new LegacyCsvPriceFeed();
            adapter = new LegacyCsvPriceFeedAdapter(legacyFeed);
        }

        @Test
        @DisplayName("Should successfully adapt valid legacy CSV quotes into BigDecimal")
        void testSuccessfulQuoteAdaptation() {
            BigDecimal aaplPrice = adapter.getLatestPrice("AAPL");
            assertEquals(new BigDecimal("185.50"), aaplPrice);

            BigDecimal btcPrice = adapter.getLatestPrice("BTC");
            assertEquals(new BigDecimal("67500.00"), btcPrice);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quote is not confirmed or not found")
        void testUnconfirmedQuoteThrows() {
            assertThrows(IllegalArgumentException.class, () -> adapter.getLatestPrice("UNKNOWN_XYZ"));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when numeric price payload is corrupt")
        void testCorruptedPricePayloadThrows() {
            legacyFeed.setLegacyPrice("CORRUPT", "NOT_A_NUMBER", "USD");
            assertThrows(IllegalStateException.class, () -> adapter.getLatestPrice("CORRUPT"));
        }
    }

    // =========================================================================
    // 2. FLYWEIGHT PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("2. Flyweight Pattern (ExchangeMetadata & Factory)")
    class FlyweightTests {

        @Test
        @DisplayName("Should return the exact same ExchangeMetadata reference for repeated queries")
        void testExchangeFlyweightReferenceIdentity() {
            ExchangeMetadata ex1 = ExchangeFlyweightFactory.getExchange("XNAS");
            ExchangeMetadata ex2 = ExchangeFlyweightFactory.getExchange("XNAS");
            ExchangeMetadata ex3 = ExchangeFlyweightFactory.getExchange("xnas"); // case insensitive

            assertSame(ex1, ex2, "Repeated queries for XNAS must share identical instance reference");
            assertSame(ex1, ex3, "Case-insensitive lookup must share identical instance reference");
            assertEquals("NASDAQ Stock Market", ex1.getExchangeName());
            assertEquals("USD", ex1.getBaseCurrency());
        }

        @Test
        @DisplayName("Should dynamically create and pool unknown OTC markets on demand")
        void testDynamicOtcExchangeFlyweight() {
            ExchangeMetadata otc = ExchangeFlyweightFactory.getExchange("MOLDOVA_SE");
            ExchangeMetadata otcAgain = ExchangeFlyweightFactory.getExchange("MOLDOVA_SE");

            assertSame(otc, otcAgain);
            assertEquals("MOLDOVA_SE", otc.getMicCode());
        }
    }

    // =========================================================================
    // 3. COMPOSITE PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("3. Composite Pattern (PortfolioComposite & AssetLeaf)")
    class CompositeTests {

        @Test
        @DisplayName("Leaf node should accurately calculate value and forbid child addition")
        void testLeafNodeBehavior() {
            ExchangeMetadata nasdaq = ExchangeFlyweightFactory.getExchange("XNAS");
            AssetLeaf leaf = new AssetLeaf("L1", "AAPL", "Apple Inc.", 50.0, new BigDecimal("180.00"), nasdaq);

            assertEquals(1, leaf.getAssetCount());
            assertEquals(new BigDecimal("9000.00"), leaf.calculateFairValue());
            assertFalse(leaf.isComposite());

            assertThrows(UnsupportedOperationException.class, () -> leaf.add(leaf));
            assertThrows(UnsupportedOperationException.class, () -> leaf.remove(leaf));
        }

        @Test
        @DisplayName("Composite should recursively compute total fair value and total asset count")
        void testCompositeRecursiveValuation() {
            ExchangeMetadata nasdaq = ExchangeFlyweightFactory.getExchange("XNAS");
            ExchangeMetadata par = ExchangeFlyweightFactory.getExchange("XPAR");

            // Sub-portfolio 1: Tech
            PortfolioComposite techFund = new PortfolioComposite("SUB1", "Tech Basket", "USD");
            techFund.add(new AssetLeaf("T1", "AAPL", "Apple", 10.0, new BigDecimal("100.00"), nasdaq)); // 1000.00
            techFund.add(new AssetLeaf("T2", "MSFT", "Microsoft", 5.0, new BigDecimal("200.00"), nasdaq)); // 1000.00

            // Sub-portfolio 2: EU
            PortfolioComposite euFund = new PortfolioComposite("SUB2", "EU Basket", "EUR");
            euFund.add(new AssetLeaf("E1", "SAP", "SAP SE", 20.0, new BigDecimal("150.00"), par)); // 3000.00

            // Master fund
            PortfolioComposite masterFund = new PortfolioComposite("MASTER", "Global Fund", "USD");
            masterFund.add(techFund);
            masterFund.add(euFund);

            assertTrue(masterFund.isComposite());
            assertEquals(3, masterFund.getAssetCount());
            // 1000 + 1000 + 3000 = 5000.00
            assertEquals(new BigDecimal("5000.00"), masterFund.calculateFairValue());

            // Remove a sub-fund
            masterFund.remove(euFund);
            assertEquals(2, masterFund.getAssetCount());
            assertEquals(new BigDecimal("2000.00"), masterFund.calculateFairValue());
        }
    }

    // =========================================================================
    // 4. DECORATOR PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("4. Decorator Pattern (WithholdingTax, Fee, Hedging)")
    class DecoratorTests {

        private PortfolioComponent basePortfolio;

        @BeforeEach
        void setUp() {
            ExchangeMetadata exchange = ExchangeFlyweightFactory.getExchange("XNAS");
            PortfolioComposite composite = new PortfolioComposite("P1", "Alpha Portfolio", "USD");
            composite.add(new AssetLeaf("A1", "AAPL", "Apple", 100.0, new BigDecimal("100.00"), exchange)); // $10,000.00
            basePortfolio = composite;
        }

        @Test
        @DisplayName("WithholdingTaxDecorator should accurately deduct capital gains tax")
        void testWithholdingTaxDecorator() {
            WithholdingTaxDecorator taxDecorator = new WithholdingTaxDecorator(basePortfolio, new BigDecimal("15.00"));

            assertEquals(new BigDecimal("1500.00"), taxDecorator.calculateTaxAmount());
            // $10,000.00 - $1,500.00 = $8,500.00
            assertEquals(new BigDecimal("8500.00"), taxDecorator.calculateFairValue());
            assertEquals(1, taxDecorator.getAssetCount());
        }

        @Test
        @DisplayName("ManagementFeeDecorator should accurately deduct annual custody fee")
        void testManagementFeeDecorator() {
            ManagementFeeDecorator feeDecorator = new ManagementFeeDecorator(basePortfolio, new BigDecimal("2.00"));

            assertEquals(new BigDecimal("200.00"), feeDecorator.calculateFeeAmount());
            // $10,000.00 - $200.00 = $9,800.00
            assertEquals(new BigDecimal("9800.00"), feeDecorator.calculateFairValue());
        }

        @Test
        @DisplayName("Decorators should chain dynamically (Fee -> Tax -> Hedging)")
        void testDecoratorChaining() {
            // Step 1: Base = $10,000.00
            // Step 2: 2% Fee -> $10,000 - $200 = $9,800.00
            PortfolioComponent feeWrapped = new ManagementFeeDecorator(basePortfolio, new BigDecimal("2.00"));
            assertEquals(new BigDecimal("9800.00"), feeWrapped.calculateFairValue());

            // Step 3: 10% Tax on post-fee -> $9,800 - $980 = $8,820.00
            PortfolioComponent taxWrapped = new WithholdingTaxDecorator(feeWrapped, new BigDecimal("10.00"));
            assertEquals(new BigDecimal("8820.00"), taxWrapped.calculateFairValue());

            // Step 4: 1% FX Hedge on post-tax -> $8,820 - $88.20 = $8,731.80
            PortfolioComponent fullyWrapped = new CurrencyHedgingDecorator(taxWrapped, new BigDecimal("1.00"), "EUR");
            assertEquals(new BigDecimal("8731.80"), fullyWrapped.calculateFairValue());
        }
    }

    // =========================================================================
    // 5. PROXY PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("5. Proxy Pattern (CachedAuditedMarketDataProxy)")
    class ProxyTests {

        @Test
        @DisplayName("Proxy should cache remote price lookups within TTL and record audit logs")
        void testCachingAndAuditLogging() {
            LegacyCsvPriceFeed legacyFeed = new LegacyCsvPriceFeed();
            LegacyCsvPriceFeedAdapter adapter = new LegacyCsvPriceFeedAdapter(legacyFeed);
            RemoteMarketDataService remoteService = new RemoteMarketDataService(adapter);

            CachedAuditedMarketDataProxy proxy = new CachedAuditedMarketDataProxy(remoteService, 2000L);

            // Call 1: Cache Miss -> Remote Call
            BigDecimal p1 = proxy.fetchLivePrice("AAPL", "Auditor_1");
            assertEquals(new BigDecimal("185.50"), p1);
            assertEquals(1, remoteService.getNetworkCallCount());
            assertEquals(0, proxy.getCacheHitCount());

            // Call 2: Cache Hit
            BigDecimal p2 = proxy.fetchLivePrice("AAPL", "Auditor_2");
            assertEquals(new BigDecimal("185.50"), p2);
            assertEquals(1, remoteService.getNetworkCallCount(), "Remote network call count must not increase on cache hit");
            assertEquals(1, proxy.getCacheHitCount());

            // Verify Audit Trail
            List<CachedAuditedMarketDataProxy.AuditLogEntry> auditTrail = proxy.getAuditTrail();
            assertEquals(2, auditTrail.size());
            assertEquals("REMOTE_FETCH", auditTrail.get(0).resolutionSource());
            assertEquals("CACHE_HIT", auditTrail.get(1).resolutionSource());
        }
    }

    // =========================================================================
    // 6. BRIDGE PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("6. Bridge Pattern (Reports & Renderers)")
    class BridgeTests {

        private PortfolioComponent sampleComponent;

        @BeforeEach
        void setUp() {
            ExchangeMetadata nasdaq = ExchangeFlyweightFactory.getExchange("XNAS");
            sampleComponent = new AssetLeaf("TST-1", "AAPL", "Apple Inc.", 10.0, new BigDecimal("150.00"), nasdaq);
        }

        @Test
        @DisplayName("ExecutiveSummaryReport with Tabular Renderer should format console output")
        void testExecutiveSummaryTabular() {
            ExecutiveSummaryReport report = new ExecutiveSummaryReport(new ConsoleTabularRenderer());
            String output = report.generate(sampleComponent);

            assertTrue(output.contains("EXECUTIVE PORTFOLIO SUMMARY"));
            assertTrue(output.contains("1500.00"));
        }

        @Test
        @DisplayName("DetailedAuditReport with JsonReportRenderer should format JSON output")
        void testDetailedAuditJson() {
            DetailedAuditReport report = new DetailedAuditReport(new JsonReportRenderer());
            String output = report.generate(sampleComponent);

            assertTrue(output.contains("\"reportType\": \"Comprehensive Audit & Risk Valuation\""));
            assertTrue(output.contains("\"fairValue\": 1500.00"));
            assertTrue(output.contains("\"status\": \"SUCCESS\""));
        }
    }

    // =========================================================================
    // 7. FACADE PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("7. Facade Pattern (InvestmentEngineFacade)")
    class FacadeTests {

        @Test
        @DisplayName("Facade should coordinate entire asset creation, decoration, and reporting workflow")
        void testFacadeOrchestration() {
            InvestmentEngineFacade facade = new InvestmentEngineFacade();

            // 1. Build composite global fund
            PortfolioComposite fund = facade.buildDemoGlobalFund("SystemTester");
            assertNotNull(fund);
            assertTrue(fund.getAssetCount() > 0);
            assertTrue(fund.calculateFairValue().compareTo(BigDecimal.ZERO) > 0);

            // 2. Apply fees and taxes via facade
            PortfolioComponent decorated = facade.applyFeeAndTaxPillars(fund, new BigDecimal("1.00"), new BigDecimal("10.00"));
            assertTrue(decorated.calculateFairValue().compareTo(fund.calculateFairValue()) < 0);

            // 3. Generate reports via facade
            String consoleReport = facade.generateReport(decorated, false, false);
            assertNotNull(consoleReport);
            assertTrue(consoleReport.contains("EXECUTIVE PORTFOLIO SUMMARY"));

            String jsonReport = facade.generateReport(decorated, true, true);
            assertNotNull(jsonReport);
            assertTrue(jsonReport.contains("\"status\": \"SUCCESS\""));
        }
    }
}
