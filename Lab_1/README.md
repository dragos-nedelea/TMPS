# TMPS Laboratory Work 1: Creational Design Patterns
**Course:** Software Architecture and Design Patterns (TMPS)  
**Institution:** Technical University of Moldova (UTM)  
**Author:** Daniel Nedelea  
**Language:** Pure Java 17+ (No external runtime frameworks)

---

## 1. Objectives

1. **Study and understand the Creational Design Patterns** and their role in abstracting, optimizing, and controlling the instantiation process.
2. **Select an appropriate domain area** and model its core entities, attributes, and relationships.
3. **Implement at least 3 Creational Design Patterns** (this project implements **all 6 classical creational patterns**: *Singleton, Builder, Factory Method, Abstract Factory, Prototype,* and *Object Pool*).
4. **Structure the project cleanly** into modular packages (`client`, `domain.models`, `domain.factory`, `domain.builder`, `domain.singleton`, `domain.prototype`, `domain.pool`) adhering to pure object-oriented design and SOLID principles.
5. **Verify correctness** through automated JUnit 5 test coverage and an interactive client demonstration.

---

## 2. Domain Description

The chosen domain is an **Institutional Investment Portfolio & Financial Asset Management Engine**.

In financial systems, asset instantiation is complex and subject to strict invariants:
- Market feeds and FX exchange rates must be synchronized across all calculations (**Singleton**).
- Portfolio entities have numerous optional and mandatory parameters requiring step-by-step assembly and invariant verification (**Builder**).
- Different asset classes (Stocks, Cryptocurrencies, P2P Loans) require specialized mathematical models and parameters (**Factory Method**).
- Different financial jurisdictions (US SEC/FINRA vs European Union MiFID II/MiCA) enforce regional instrument suites, currencies, and regulatory disclosures (**Abstract Factory**).
- Standard benchmark assets (e.g. S&P 500 tech blue-chips, AAA credit tranches) share complex baseline configurations, making template cloning vastly more efficient than manual re-specification (**Prototype**).
- High-frequency portfolio valuation and stress-testing simulations require heavy calculation buffers that induce high Garbage Collection (GC) pauses if created repeatedly (**Object Pool**).

---

## 3. Implemented Creational Design Patterns

| Pattern | Component | Problem Solved |
| :--- | :--- | :--- |
| **Singleton** | `MarketDataFeed` | Guarantees a single, thread-safe source of truth for foreign exchange conversion rates, benchmark indices, and risk-free rates across the entire application. |
| **Builder** | `PortfolioBuilder` | Prevents telescopic constructors and invalid parameter combinations; enforces step-by-step invariant validation before creating an immutable `Portfolio`. |
| **Factory Method** | `AssetFactory<T>`, `StockAssetFactory`, `CryptoAssetFactory`, `LoanAssetFactory` | Encapsulates the instantiation of specialized asset subclasses while decoupling client code from concrete implementations. |
| **Abstract Factory** | `FinancialMarketFactory`, `USMarketFactory`, `EuropeanMarketFactory` | Produces coordinated families of related regional financial products (Equities, Debt, Digital Assets) tied to specific jurisdictions (USD vs EUR). |
| **Prototype** | `Asset.cloneAsset()`, `AssetPrototypeRegistry` | Enables rapid cloning and customization of pre-configured benchmark asset templates without invoking complex constructors from scratch. |
| **Object Pool** | `ValuationWorkerPool`, `ValuationWorker` | Recycles pre-allocated, memory-intensive calculator workers to eliminate allocation overhead and GC latency during frequent portfolio stress appraisals. |

---

## 4. Package Structure

```
Lab_1/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── tmps/
    │           └── portfolio/
    │               ├── Main.java                                   # Entry point & Composition Root
    │               ├── client/
    │               │   └── PortfolioApplication.java               # Execution scenario & demonstration
    │               └── domain/
    │                   ├── models/                                 # Domain entities & contracts
    │                   │   ├── Asset.java                          # Asset interface extending Cloneable
    │                   │   ├── AssetType.java                      # Category enumeration
    │                   │   ├── StockAsset.java                     # Public corporate equity holding
    │                   │   ├── CryptoAsset.java                    # Digital token / cryptocurrency holding
    │                   │   ├── P2PLoanAsset.java                   # Fixed-income consumer debt holding
    │                   │   └── Portfolio.java                      # Consolidated investor portfolio container
    │                   ├── singleton/
    │                   │   └── MarketDataFeed.java                 # Bill Pugh thread-safe Singleton
    │                   ├── builder/
    │                   │   └── PortfolioBuilder.java               # Fluent invariant-validating builder
    │                   ├── factory/                                # Factory Method pattern
    │                   │   ├── AssetFactory.java                   # Abstract creator
    │                   │   ├── StockAssetFactory.java              # Concrete creator for stocks
    │                   │   ├── CryptoAssetFactory.java             # Concrete creator for crypto
    │                   │   ├── LoanAssetFactory.java               # Concrete creator for P2P debt
    │                   │   └── regional/                           # Abstract Factory pattern
    │                   │       ├── FinancialMarketFactory.java     # Abstract factory interface
    │                   │       ├── USMarketFactory.java            # Concrete factory (USD / SEC suite)
    │                   │       └── EuropeanMarketFactory.java      # Concrete factory (EUR / MiFID suite)
    │                   ├── prototype/                              # Prototype pattern
    │                   │   └── AssetPrototypeRegistry.java         # Benchmark prototype catalog & cloner
    │                   └── pool/                                   # Object Pool pattern
    │                       ├── ValuationWorker.java                # Recyclable worker resource
    │                       └── ValuationWorkerPool.java            # Thread-safe worker queue & manager
    └── test/
        └── java/
            └── tmps/
                └── portfolio/
                    └── CreationalPatternsTest.java                 # 16 automated JUnit 5 tests
```

---

## 5. In-Depth Pattern Implementations

### 5.1 Singleton Pattern (`MarketDataFeed.java`)
Implemented using the **Bill Pugh Initialization-on-demand Holder Idiom**. The static inner class `Holder` is only loaded when `getInstance()` is explicitly invoked, ensuring zero runtime synchronization overhead while relying on JVM class-loading guarantees for thread-safety.

```java
public final class MarketDataFeed {
    private final Map<String, BigDecimal> exchangeRatesToUsd = new ConcurrentHashMap<>();
    
    private MarketDataFeed() {
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("MarketDataFeed instance already exists.");
        }
        exchangeRatesToUsd.put("USD", BigDecimal.ONE);
        exchangeRatesToUsd.put("EUR", new BigDecimal("1.0850"));
    }

    private static class Holder {
        private static final MarketDataFeed INSTANCE = new MarketDataFeed();
    }

    public static MarketDataFeed getInstance() {
        return Holder.INSTANCE;
    }
}
```

---

### 5.2 Builder Pattern (`PortfolioBuilder.java`)
Eliminates telescopic constructor anti-patterns. Guarantees that invalid domain states (e.g., blank investor names, invalid emails, negative cash balance) cannot be created.

```java
Portfolio portfolio = new PortfolioBuilder()
        .withId("PORTFOLIO-ALPHA")
        .withInvestor("Daniel Nedelea", "daniel.nedelea@isa.utm.md")
        .withRiskProfile(Portfolio.RiskProfile.GROWTH)
        .withBaseCurrency("USD")
        .withCashBalance(new BigDecimal("25000.00"))
        .addAsset(appleStock)
        .addTag("Tech-Focus")
        .build();
```

---

### 5.3 Factory Method Pattern (`AssetFactory<T>`)
Defines an abstract creation method `createAsset(...)` implemented by specialized creator classes (`StockAssetFactory`, `CryptoAssetFactory`, `LoanAssetFactory`). Clients program against the abstract creator without directly coupling to constructor signatures or pricing defaults.

```java
public abstract class AssetFactory<T extends Asset> {
    public abstract T createAsset(String id, String symbolOrRef, double quantity, double priceOrRate);

    public T issueAsset(String id, String symbolOrRef, double quantity, double priceOrRate) {
        T asset = createAsset(id, symbolOrRef, quantity, priceOrRate);
        System.out.printf("Issued %s [%s]%n", asset.getType(), asset.getName());
        return asset;
    }
}
```

---

### 5.4 Abstract Factory Pattern (`FinancialMarketFactory`)
Coordinates the creation of families of related financial instruments tailored to specific jurisdictions. For example, `USMarketFactory` instantiates USD-denominated products on NASDAQ/NYSE, while `EuropeanMarketFactory` instantiates EUR-denominated products compliant with ESMA and MiFID II.

```java
public interface FinancialMarketFactory {
    String getJurisdiction();
    String getMarketCurrency();
    StockAsset createBenchmarkEquity(String id, double quantity);
    P2PLoanAsset createConsumerCredit(String id, String borrowerRef, double principal);
    CryptoAsset createCompliantCrypto(String id, double amount);
}
```

---

### 5.5 Prototype Pattern (`AssetPrototypeRegistry.java`)
Allows cloning complex, pre-vetted financial assets directly from memory rather than repeating extensive parameter validation and calculation configurations.

```java
// Clone an S&P 500 Microsoft template and customize holding quantity and ID
StockAsset customStock = prototypeRegistry.cloneStock("SP500_MSFT", "MY-MSFT-01", 50.0);
```

---

### 5.6 Object Pool Pattern (`ValuationWorkerPool.java`)
Manages a fixed collection of reusable `ValuationWorker` instances via a thread-safe `BlockingQueue`. Eliminates CPU and memory allocation churn during heavy portfolio analysis:

```java
ValuationWorkerPool pool = new ValuationWorkerPool(2);
ValuationWorker worker = pool.acquire(500); // Wait up to 500ms
try {
    ValuationReport report = worker.executeValuation(portfolio);
} finally {
    pool.release(worker); // State reset and returned to pool
}
```

---

## 6. How to Compile and Run

### Running the Live Demonstration
Execute from the `Lab_1` directory using Maven:
```bash
mvn compile exec:java
```

### Running Automated Unit Tests
Execute the full test suite:
```bash
mvn test
```

Expected result:
```text
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 7. Conclusions

By implementing the creational design patterns in this project:
1. **Object creation logic was entirely decoupled** from core business execution, satisfying the Single Responsibility and Open/Closed principles.
2. **System consistency and thread-safety** were guaranteed via the Bill Pugh Singleton for market data and the Object Pool for reusable calculation contexts.
3. **Domain invariant safety and extensibility** were achieved using the Builder and Prototype patterns, dramatically simplifying client-side construction and cloning of intricate financial portfolios.
