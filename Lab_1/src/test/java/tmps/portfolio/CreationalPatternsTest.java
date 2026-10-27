package tmps.portfolio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tmps.portfolio.domain.builder.PortfolioBuilder;
import tmps.portfolio.domain.factory.AssetFactory;
import tmps.portfolio.domain.factory.CryptoAssetFactory;
import tmps.portfolio.domain.factory.LoanAssetFactory;
import tmps.portfolio.domain.factory.StockAssetFactory;
import tmps.portfolio.domain.factory.regional.EuropeanMarketFactory;
import tmps.portfolio.domain.factory.regional.FinancialMarketFactory;
import tmps.portfolio.domain.factory.regional.USMarketFactory;
import tmps.portfolio.domain.models.Asset;
import tmps.portfolio.domain.models.AssetType;
import tmps.portfolio.domain.models.CryptoAsset;
import tmps.portfolio.domain.models.P2PLoanAsset;
import tmps.portfolio.domain.models.Portfolio;
import tmps.portfolio.domain.models.StockAsset;
import tmps.portfolio.domain.pool.ValuationWorker;
import tmps.portfolio.domain.pool.ValuationWorkerPool;
import tmps.portfolio.domain.prototype.AssetPrototypeRegistry;
import tmps.portfolio.domain.singleton.MarketDataFeed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lab 1: Creational Design Patterns Unit Tests")
public class CreationalPatternsTest {

    // =========================================================================
    // 1. SINGLETON PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("1. Singleton Pattern (MarketDataFeed)")
    class SingletonTests {

        @Test
        @DisplayName("getInstance() should always return the identical instance")
        void testSingletonIdentity() {
            MarketDataFeed instance1 = MarketDataFeed.getInstance();
            MarketDataFeed instance2 = MarketDataFeed.getInstance();

            assertNotNull(instance1);
            assertSame(instance1, instance2, "Both references must point to the identical memory instance");
        }

        @Test
        @DisplayName("Singleton must guarantee thread-safety across concurrent access")
        void testConcurrentSingletonAccess() throws InterruptedException {
            int threadCount = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<MarketDataFeed> instances = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        instances.add(MarketDataFeed.getInstance());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertEquals(threadCount, instances.size());
            MarketDataFeed first = instances.get(0);
            for (MarketDataFeed current : instances) {
                assertSame(first, current, "Every concurrent thread must receive the exact same singleton instance");
            }
        }

        @Test
        @DisplayName("Singleton accurately converts currencies via triangulated FX rates")
        void testCurrencyConversion() {
            MarketDataFeed feed = MarketDataFeed.getInstance();
            BigDecimal eurAmount = new BigDecimal("100.00");
            BigDecimal usdAmount = feed.convertCurrency(eurAmount, "EUR", "USD");

            // 100 EUR * 1.0850 = 108.50 USD
            assertEquals(new BigDecimal("108.50"), usdAmount);
        }
    }

    // =========================================================================
    // 2. BUILDER PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("2. Builder Pattern (PortfolioBuilder)")
    class BuilderTests {

        @Test
        @DisplayName("Successfully builds a valid Portfolio with all invariants satisfied")
        void testSuccessfulBuild() {
            StockAsset stock = new StockAsset("S1", "AAPL", "Apple Inc.", "NASDAQ",
                    BigDecimal.valueOf(10), BigDecimal.valueOf(150), BigDecimal.ZERO, "USD");

            Portfolio portfolio = new PortfolioBuilder()
                    .withId("PF-001")
                    .withInvestor("Alice Smith", "alice@test.com")
                    .withRiskProfile(Portfolio.RiskProfile.AGGRESSIVE)
                    .withBaseCurrency("USD")
                    .withCashBalance(new BigDecimal("5000.00"))
                    .addAsset(stock)
                    .addTag("Technology")
                    .build();

            assertEquals("PF-001", portfolio.getId());
            assertEquals("Alice Smith", portfolio.getInvestorName());
            assertEquals("alice@test.com", portfolio.getInvestorEmail());
            assertEquals(Portfolio.RiskProfile.AGGRESSIVE, portfolio.getRiskProfile());
            assertEquals("USD", portfolio.getBaseCurrency());
            assertEquals(new BigDecimal("5000.00"), portfolio.getCashBalance());
            assertEquals(1, portfolio.getAssets().size());
            assertTrue(portfolio.getTags().contains("technology"));
            assertEquals(new BigDecimal("6500.00"), portfolio.calculateNetWorth());
        }

        @Test
        @DisplayName("Rejects build when mandatory ID is missing")
        void testMissingIdThrowsException() {
            PortfolioBuilder builder = new PortfolioBuilder()
                    .withInvestor("Bob", "bob@test.com");

            IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
            assertTrue(ex.getMessage().contains("Portfolio ID is mandatory"));
        }

        @Test
        @DisplayName("Rejects build when email format is invalid")
        void testInvalidEmailThrowsException() {
            PortfolioBuilder builder = new PortfolioBuilder()
                    .withId("PF-002")
                    .withInvestor("Charlie", "invalid-email-format");

            IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
            assertTrue(ex.getMessage().contains("Valid investor email is required"));
        }

        @Test
        @DisplayName("Rejects build when cash balance is negative")
        void testNegativeCashThrowsException() {
            PortfolioBuilder builder = new PortfolioBuilder()
                    .withId("PF-003")
                    .withInvestor("Diana", "diana@test.com")
                    .withCashBalance(new BigDecimal("-50.00"));

            IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
            assertTrue(ex.getMessage().contains("Cash balance cannot be negative"));
        }
    }

    // =========================================================================
    // 3. FACTORY METHOD PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("3. Factory Method Pattern (AssetFactory subclasses)")
    class FactoryMethodTests {

        @Test
        @DisplayName("StockAssetFactory instantiates properly typed and enriched StockAsset")
        void testStockAssetFactory() {
            AssetFactory<StockAsset> factory = new StockAssetFactory("NYSE", "USD");
            StockAsset asset = factory.createAsset("STK-10", "AAPL", 25.0, 180.0);

            assertEquals("STK-10", asset.getId());
            assertEquals("AAPL", asset.getTicker());
            assertEquals("Apple Inc.", asset.getCompanyName());
            assertEquals("NYSE", asset.getExchange());
            assertEquals(AssetType.PUBLIC_EQUITY, asset.getType());
            assertTrue(asset.calculateFairValue().compareTo(BigDecimal.valueOf(4500)) > 0);
        }

        @Test
        @DisplayName("CryptoAssetFactory instantiates properly configured CryptoAsset")
        void testCryptoAssetFactory() {
            AssetFactory<CryptoAsset> factory = new CryptoAssetFactory("USD");
            CryptoAsset crypto = factory.createAsset("CRP-10", "BTC", 0.5, 60000.0);

            assertEquals("CRP-10", crypto.getId());
            assertEquals("BTC", crypto.getSymbol());
            assertEquals("Bitcoin", crypto.getNetworkName());
            assertEquals(AssetType.CRYPTO_TOKEN, crypto.getType());
            assertEquals(new BigDecimal("29987.50"), crypto.calculateFairValue()); // 30000 - 12.50 gas
        }

        @Test
        @DisplayName("LoanAssetFactory instantiates properly configured P2PLoanAsset")
        void testLoanAssetFactory() {
            AssetFactory<P2PLoanAsset> factory = new LoanAssetFactory("PeerPlatform", "USD");
            P2PLoanAsset loan = factory.createAsset("LN-10", "BORR-1234", 10000.0, 10.0);

            assertEquals("LN-10", loan.getId());
            assertEquals("BORR-1234", loan.getBorrowerId());
            assertEquals("PeerPlatform", loan.getPlatform());
            assertEquals(AssetType.P2P_DEBT, loan.getType());
            assertTrue(loan.calculateFairValue().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    // =========================================================================
    // 4. ABSTRACT FACTORY PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("4. Abstract Factory Pattern (Regional Market Product Suites)")
    class AbstractFactoryTests {

        @Test
        @DisplayName("USMarketFactory creates coordinated USD product family")
        void testUSMarketFactory() {
            FinancialMarketFactory factory = new USMarketFactory();
            assertEquals("USD", factory.getMarketCurrency());

            StockAsset equity = factory.createBenchmarkEquity("US-EQ", 10);
            P2PLoanAsset debt = factory.createConsumerCredit("US-LN", "B-1", 5000);
            CryptoAsset crypto = factory.createCompliantCrypto("US-CRP", 1.0);

            assertEquals("USD", equity.getCurrency());
            assertEquals("USD", debt.getCurrency());
            assertEquals("USD", crypto.getCurrency());
            assertEquals("NASDAQ", equity.getExchange());
        }

        @Test
        @DisplayName("EuropeanMarketFactory creates coordinated EUR product family")
        void testEuropeanMarketFactory() {
            FinancialMarketFactory factory = new EuropeanMarketFactory();
            assertEquals("EUR", factory.getMarketCurrency());

            StockAsset equity = factory.createBenchmarkEquity("EU-EQ", 10);
            P2PLoanAsset debt = factory.createConsumerCredit("EU-LN", "B-2", 5000);
            CryptoAsset crypto = factory.createCompliantCrypto("EU-CRP", 1.0);

            assertEquals("EUR", equity.getCurrency());
            assertEquals("EUR", debt.getCurrency());
            assertEquals("EUR", crypto.getCurrency());
            assertTrue(equity.getExchange().contains("Euronext"));
        }
    }

    // =========================================================================
    // 5. PROTOTYPE PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("5. Prototype Pattern (Asset Cloning & Registry)")
    class PrototypeTests {

        @Test
        @DisplayName("Asset clone operation produces an independent deep copy")
        void testCloneIndependence() {
            StockAsset original = new StockAsset("ORIG-1", "NVDA", "Nvidia Corp", "NASDAQ",
                    BigDecimal.valueOf(100), BigDecimal.valueOf(120), BigDecimal.ZERO, "USD");

            StockAsset cloned = original.cloneAsset();

            assertNotSame(original, cloned, "Clone must be a distinct object in heap memory");
            assertEquals(original.getId(), cloned.getId());
            assertEquals(original.calculateFairValue(), cloned.calculateFairValue());
        }

        @Test
        @DisplayName("Prototype registry allows cloning and customizing benchmark templates")
        void testPrototypeRegistryCustomization() {
            AssetPrototypeRegistry registry = new AssetPrototypeRegistry();

            StockAsset customStock = registry.cloneStock("SP500_MSFT", "MY-MSFT", 50.0);
            assertEquals("MY-MSFT", customStock.getId());
            assertEquals(BigDecimal.valueOf(50.0), customStock.getShareCount());
            assertEquals("MSFT", customStock.getTicker());

            CryptoAsset customCrypto = registry.cloneCrypto("CRYPTO_BTC_COLD_STORAGE", "MY-BTC", 2.5);
            assertEquals("MY-BTC", customCrypto.getId());
            assertEquals(BigDecimal.valueOf(2.5), customCrypto.getCoinAmount());
        }
    }

    // =========================================================================
    // 6. OBJECT POOL PATTERN TESTS
    // =========================================================================
    @Nested
    @DisplayName("6. Object Pool Pattern (ValuationWorkerPool)")
    class ObjectPoolTests {

        @Test
        @DisplayName("Workers can be acquired, reused, and reset cleanly")
        void testWorkerAcquireAndRelease() throws InterruptedException {
            ValuationWorkerPool pool = new ValuationWorkerPool(1);
            assertEquals(1, pool.getAvailableCount());
            assertEquals(0, pool.getInUseCount());

            ValuationWorker w1 = pool.acquire(500);
            assertNotNull(w1);
            assertEquals(0, pool.getAvailableCount());
            assertEquals(1, pool.getInUseCount());

            pool.release(w1);
            assertEquals(1, pool.getAvailableCount());
            assertEquals(0, pool.getInUseCount());

            // Re-acquiring from single-worker pool guarantees returning the recycled worker
            ValuationWorker w2 = pool.acquire(500);
            assertSame(w1, w2, "Object pool must recycle and return previously released worker");
            pool.release(w2);
        }

        @Test
        @DisplayName("Exhausted pool throws IllegalStateException after timeout")
        void testExhaustedPoolThrowsException() throws InterruptedException {
            ValuationWorkerPool pool = new ValuationWorkerPool(1);
            ValuationWorker w1 = pool.acquire(200);

            assertThrows(IllegalStateException.class, () -> pool.acquire(100),
                    "Should throw IllegalStateException when no worker is available within timeout");

            pool.release(w1);
        }
    }
}
