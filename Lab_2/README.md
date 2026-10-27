# TMPS Laboratory Work 2: Structural Design Patterns
**Course:** Software Architecture and Design Patterns (TMPS)  
**Institution:** Technical University of Moldova (UTM)  
**Author:** Daniel Nedelea  
**Language:** Pure Java 17+ (No external runtime frameworks)

---

## 1. Objectives

1. **Study and understand the Structural Design Patterns** and how classes and objects are composed to form larger, more flexible structures.
2. **Continue the established domain** of the *Institutional Investment Portfolio & Financial Asset Management Engine* from the previous laboratory works.
3. **Implement structural design patterns** from the required catalog (this project implements **all 7 classical structural patterns**: *Adapter, Bridge, Composite, Decorator, Facade, Flyweight,* and *Proxy*).
4. **Bury object creation and composition mechanisms** into functionalities/subsystems instead of exposing them directly in the client.
5. **Ensure a single client for the entire system** (`PortfolioApplication.java`), maintaining a clean separation of concerns and clear responsibility segregation.
6. **Verify correctness** through automated JUnit 5 test coverage and an interactive console client demonstration.

---

## 2. Theoretical Background

In software engineering, **Structural Design Patterns** focus on class and object composition. While structural class patterns rely on inheritance to compose interfaces or implementations, structural object patterns leverage **object composition**—a far more flexible mechanism that allows dynamic restructuring at runtime without subclass explosion.

Key structural patterns address fundamental architectural challenges:
- **Adapter:** Bridges interfaces that are incompatible due to differing data formats, protocols, or legacy signatures.
- **Bridge:** Separates a high-level abstraction from its platform-specific or presentation-specific implementations so both can vary independently.
- **Composite:** Composes objects into tree structures to represent part-whole hierarchies, allowing clients to treat individual objects and compositions uniformly.
- **Decorator:** Attaches additional responsibilities and dynamic adjustments to objects without subclassing or modifying underlying code.
- **Facade:** Provides a simplified, unified high-level interface to a complex library, subsystem, or cluster of classes.
- **Flyweight:** Minimizes memory usage by sharing common immutable intrinsic state among multiple fine-grained objects.
- **Proxy:** Provides a surrogate or placeholder for another object to control access, introduce caching, or maintain audit logging.

---

## 3. Domain Description & Structural Pattern Mapping

The chosen domain is an **Institutional Investment Portfolio & Financial Asset Management Engine**.

In high-finance operations, structural composition challenges arise at every stage of the lifecycle:

| Pattern | Component | Problem Solved in Financial Domain |
| :--- | :--- | :--- |
| **Facade** | `InvestmentEngineFacade` | Serves as the single point of entry for clients, encapsulating complex interactions between adapters, proxies, composite builders, and decorators. |
| **Adapter** | `LegacyCsvPriceFeedAdapter`, `LegacyCsvPriceFeed` | Adapts incompatible legacy third-party batch price feeds (raw semicolon-separated CSV strings) into typed domain `BigDecimal` quotes. |
| **Composite** | `PortfolioComponent`, `PortfolioComposite`, `AssetLeaf` | Models multi-tier institutional Fund-of-Funds (FoF), regional funds, and sector baskets as recursive tree structures evaluated uniformly. |
| **Decorator** | `ValuationDecorator`, `WithholdingTaxDecorator`, `ManagementFeeDecorator`, `CurrencyHedgingDecorator` | Dynamically applies real-world financial deductions (capital gains tax, annual management/custody fees, FX risk hedging) without mutating base assets. |
| **Proxy** | `CachedAuditedMarketDataProxy`, `RemoteMarketDataService` | Intercepts remote market data lookups, providing in-memory TTL caching to cut network latency and recording regulatory audit logs. |
| **Bridge** | `PortfolioReport` (Abstraction) & `ReportRenderer` (Implementor) | Decouples report business models (`ExecutiveSummaryReport`, `DetailedAuditReport`) from multi-channel formatting (`ConsoleTabularRenderer`, `JsonReportRenderer`). |
| **Flyweight** | `ExchangeMetadata`, `ExchangeFlyweightFactory` | Pools immutable market exchange definitions (MIC code, timezone, currency, regulator) across thousands of positions to eliminate memory redundancy. |

---

## 4. Package Structure

```
Lab_2/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── tmps/
    │           └── portfolio/
    │               ├── Main.java                                   # Entry point & Composition Root
    │               ├── client/
    │               │   └── PortfolioApplication.java               # Single unified client for the system
    │               ├── facade/
    │               │   └── InvestmentEngineFacade.java             # High-level orchestration facade
    │               ├── adapter/
    │               │   ├── PriceFeedProvider.java                  # Target interface
    │               │   ├── LegacyCsvPriceFeed.java                 # Adaptee (legacy feed)
    │               │   └── LegacyCsvPriceFeedAdapter.java          # Concrete Adapter
    │               ├── flyweight/
    │               │   ├── ExchangeMetadata.java                   # Immutable Flyweight
    │               │   └── ExchangeFlyweightFactory.java           # Thread-safe Flyweight pool
    │               ├── composite/
    │               │   ├── PortfolioComponent.java                 # Base Component interface
    │               │   ├── AssetLeaf.java                          # Leaf node (individual holding)
    │               │   └── PortfolioComposite.java                 # Composite node (nested fund container)
    │               ├── decorator/
    │               │   ├── ValuationDecorator.java                 # Base Decorator
    │               │   ├── WithholdingTaxDecorator.java            # Capital gains tax deduction
    │               │   ├── ManagementFeeDecorator.java             # Custody fee deduction
    │               │   └── CurrencyHedgingDecorator.java           # FX protection adjustment
    │               ├── proxy/
    │               │   ├── MarketDataService.java                  # Subject interface
    │               │   ├── RemoteMarketDataService.java            # Real Subject
    │               │   └── CachedAuditedMarketDataProxy.java       # Proxy with Caching & Audit trail
    │               └── bridge/
    │                   ├── renderer/
    │                   │   ├── ReportRenderer.java                 # Implementor interface
    │                   │   ├── ConsoleTabularRenderer.java         # Concrete Implementor 1 (ASCII tables)
    │                   │   └── JsonReportRenderer.java             # Concrete Implementor 2 (JSON payload)
    │                   └── report/
    │                       ├── PortfolioReport.java                # Report Abstraction
    │                       ├── ExecutiveSummaryReport.java         # Refined Abstraction 1
    │                       └── DetailedAuditReport.java            # Refined Abstraction 2
    └── test/
        └── java/
            └── tmps/
                └── portfolio/
                    └── StructuralPatternsTest.java                 # 14 automated JUnit 5 tests
```

---

## 5. In-Depth Pattern Implementations

### 5.1 Facade Pattern (`InvestmentEngineFacade.java`)
**Motivation:** Rather than requiring client code to wire adapters, configure proxies, instantiate flyweights, and wrap decorators manually, the facade provides an intuitive high-level API burying all structural creation and coordination.

```java
public class InvestmentEngineFacade {
    private final MarketDataService marketDataService;
    private final CachedAuditedMarketDataProxy marketDataProxy;

    public InvestmentEngineFacade() {
        LegacyCsvPriceFeed legacyFeed = new LegacyCsvPriceFeed();
        PriceFeedProvider feedAdapter = new LegacyCsvPriceFeedAdapter(legacyFeed);
        RemoteMarketDataService remoteService = new RemoteMarketDataService(feedAdapter);
        this.marketDataProxy = new CachedAuditedMarketDataProxy(remoteService, 5000L);
        this.marketDataService = this.marketDataProxy;
    }

    public PortfolioComponent applyFeeAndTaxPillars(PortfolioComponent comp, BigDecimal fee, BigDecimal tax) {
        PortfolioComponent feeAdjusted = new ManagementFeeDecorator(comp, fee);
        return new WithholdingTaxDecorator(feeAdjusted, tax);
    }
}
```

---

### 5.2 Adapter Pattern (`LegacyCsvPriceFeedAdapter.java`)
**Motivation:** Integrates third-party market data feeds emitting legacy semicolon-delimited CSV records into our internal `PriceFeedProvider` interface.

```java
public class LegacyCsvPriceFeedAdapter implements PriceFeedProvider {
    private final LegacyCsvPriceFeed legacyFeed;

    @Override
    public BigDecimal getLatestPrice(String symbol) {
        String rawRecord = legacyFeed.fetchLegacyCsvQuote(symbol);
        // Extracts "PRICE=185.50" and verifies "STATUS=CONFIRMED"
        for (String token : rawRecord.split(";")) {
            String[] pair = token.split("=");
            if ("PRICE".equalsIgnoreCase(pair[0].trim())) {
                return new BigDecimal(pair[1].trim());
            }
        }
        throw new IllegalArgumentException("Unconfirmed quote for " + symbol);
    }
}
```

---

### 5.3 Flyweight Pattern (`ExchangeMetadata.java` & `ExchangeFlyweightFactory.java`)
**Motivation:** Institutional portfolios hold millions of transactions across a handful of global exchanges (NYSE, NASDAQ, LSE, EURONEXT). Storing full exchange details (clearing currency, timezone, regulatory body) redundantly in every single asset position would waste enormous memory. The flyweight factory shares identical immutable instances:

```java
public final class ExchangeFlyweightFactory {
    private static final Map<String, ExchangeMetadata> POOL = new ConcurrentHashMap<>();

    public static ExchangeMetadata getExchange(String micCode) {
        return POOL.computeIfAbsent(micCode.toUpperCase(), k ->
                new ExchangeMetadata(k, "Generic OTC Market (" + k + ")", "International", "UTC", "USD", "Standard Authority")
        );
    }
}
```

---

### 5.4 Composite Pattern (`PortfolioComposite.java` & `AssetLeaf.java`)
**Motivation:** Represents complex multi-asset Fund-of-Funds and sector sub-portfolios as recursive tree hierarchies. Client operations (fair value calculation, asset count, hierarchy printing) treat individual asset leaves and nested portfolios uniformly:

```java
public class PortfolioComposite implements PortfolioComponent {
    private final List<PortfolioComponent> children = new ArrayList<>();

    @Override
    public BigDecimal calculateFairValue() {
        BigDecimal total = BigDecimal.ZERO;
        for (PortfolioComponent child : children) {
            total = total.add(child.calculateFairValue());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int getAssetCount() {
        return children.stream().mapToInt(PortfolioComponent::getAssetCount).sum();
    }
}
```

---

### 5.5 Decorator Pattern (`ValuationDecorator.java` & Concrete Decorators)
**Motivation:** In financial management, base gross values must be dynamically adjusted for management fees, capital gains tax, and FX hedging. Decorators allow chaining adjustments at runtime without modifying the underlying composite structure:

```java
// Chaining 3 decorators dynamically:
PortfolioComponent feeAdjusted = new ManagementFeeDecorator(masterFund, new BigDecimal("1.25"));
PortfolioComponent taxAdjusted = new WithholdingTaxDecorator(feeAdjusted, new BigDecimal("15.00"));
PortfolioComponent fullyDecorated = new CurrencyHedgingDecorator(taxAdjusted, new BigDecimal("0.40"), "EUR");

// Automatically calculates: Gross -> Gross - Fee -> PostFee - Tax -> PostTax - Hedging
BigDecimal netValue = fullyDecorated.calculateFairValue();
```

---

### 5.6 Proxy Pattern (`CachedAuditedMarketDataProxy.java`)
**Motivation:** Intercepts live price queries to solve two real-world concerns:
1. **Performance:** In-memory caching with TTL prevents expensive remote network requests for repeated quotes.
2. **Compliance:** Every query is recorded in an immutable regulatory audit trail with timestamp, user identity, and resolution source (`CACHE_HIT` vs `REMOTE_FETCH`).

```java
public class CachedAuditedMarketDataProxy implements MarketDataService {
    @Override
    public BigDecimal fetchLivePrice(String symbol, String requester) {
        CacheRecord existing = cache.get(symbol);
        if (existing != null && (System.currentTimeMillis() - existing.cachedAtMillis()) <= ttlMillis) {
            recordAudit(requester, symbol, existing.price(), "CACHE_HIT");
            return existing.price();
        }
        BigDecimal price = realService.fetchLivePrice(symbol, requester);
        cache.put(symbol, new CacheRecord(price, System.currentTimeMillis()));
        recordAudit(requester, symbol, price, "REMOTE_FETCH");
        return price;
    }
}
```

---

### 5.7 Bridge Pattern (`PortfolioReport.java` & `ReportRenderer.java`)
**Motivation:** Prevents a Cartesian product subclass explosion between report types (`ExecutiveSummaryReport`, `DetailedAuditReport`) and rendering channels (`ConsoleTabularRenderer`, `JsonReportRenderer`). The abstraction delegates presentation formatting to the implementor:

```java
// Bridge in action:
ReportRenderer consoleRenderer = new ConsoleTabularRenderer();
ReportRenderer jsonRenderer = new JsonReportRenderer();

PortfolioReport executiveReport = new ExecutiveSummaryReport(consoleRenderer);
PortfolioReport auditReport = new DetailedAuditReport(jsonRenderer);

String consoleOutput = executiveReport.generate(portfolio);
String jsonOutput = auditReport.generate(portfolio);
```

---

## 6. How to Compile and Run

### Running the Live Demonstration
Execute from the `Lab_2` directory using Maven:
```bash
mvn compile exec:java
```

### Running Automated Unit Tests
Execute the full JUnit 5 test suite:
```bash
mvn test
```

Expected result:
```text
[INFO] Running tmps.portfolio.StructuralPatternsTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 7. Conclusions

By completing this laboratory work:
1. **Object composition was demonstrated over class inheritance:** Complex financial requirements (such as fees, taxes, and multi-currency hedging) were implemented dynamically using the Decorator and Composite patterns without fragile subclass hierarchies.
2. **Creation logic was successfully buried:** In accordance with the lab requirements, low-level factory wiring and pattern assembly were encapsulated inside `InvestmentEngineFacade`, leaving `PortfolioApplication` as the single, clean client for the entire system.
3. **Cross-cutting concerns were segregated cleanly:** The Proxy pattern isolated caching and audit compliance, the Adapter pattern decoupled legacy feed schemas, and the Bridge pattern ensured multi-channel reporting could scale independently of financial calculations.
4. **All 7 classic Structural Design Patterns** were demonstrated working together harmoniously in a realistic, production-grade financial domain.
