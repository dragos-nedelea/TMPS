# TMPS Lab 1: Investment Portfolio Valuation Engine
**Course:** Software Architecture and Design Patterns (TMPS)  
**Institution:** Technical University of Moldova (UTM)  
**Focus:** Academic Demonstration of 3 Core SOLID Principles (SRP, OCP, DIP)  
**Language:** Pure Java 17+ (No external runtime frameworks)

---

## 1. Project Overview

The **Investment Portfolio Valuation Engine** is an academic software project designed to model, evaluate, and report the fair market value of diverse financial asset classes (such as **Public Equities**, **P2P Debt Loans**, and **Cryptocurrencies**). 

The application is architected from the ground up to strictly uphold three foundational SOLID design principles:
1. **SRP (Single Responsibility Principle)**
2. **OCP (Open/Closed Principle)**
3. **DIP (Dependency Inversion Principle)**

The design avoids framework "magic" (e.g. Spring DI reflection) in favor of explicit, pure Object-Oriented constructor injection, making the architecture clear and transparent for academic defense and evaluation.

---

## 2. Architecture & Package Structure

```
Lab_1/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── tmps/
    │           └── portfolio/
    │               ├── Main.java                        # Composition Root & Demonstration
    │               ├── domain/                          # State & Invariants only (SRP)
    │               │   ├── Asset.java                   # Base asset contract
    │               │   ├── StockAsset.java              # Public equity domain record
    │               │   ├── P2PLoanAsset.java            # P2P consumer debt record
    │               │   ├── CryptoAsset.java             # Digital token record (OCP demo)
    │               │   ├── Portfolio.java               # Container of owned assets
    │               │   ├── ValuationResult.java         # Valuation outcome carrier
    │               │   └── PortfolioReport.java         # Consolidated valuation model
    │               ├── strategy/                        # Valuation logic & Strategy Pattern (OCP)
    │               │   ├── ValuationStrategy.java       # Strategy interface
    │               │   ├── StockValuationStrategy.java   # Dividend-adjusted pricing algorithm
    │               │   ├── P2PLoanValuationStrategy.java # Actuarial risk-discounted algorithm
    │               │   ├── CryptoValuationStrategy.java  # Net-of-gas-fee pricing algorithm
    │               │   ├── ValuationStrategyProvider.java # Strategy lookup abstraction (DIP)
    │               │   └── DefaultValuationStrategyRegistry.java # Dynamic strategy registry
    │               ├── repository/                      # Persistence abstractions & storage (DIP)
    │               │   ├── AssetRepository.java         # Persistence abstraction
    │               │   └── InMemoryAssetRepository.java # Low-level in-memory storage
    │               ├── export/                          # Output formatting & rendering (SRP/DIP)
    │               │   ├── ReportExporter.java          # Output abstraction
    │               │   ├── ConsoleReportExporter.java   # Formatted tabular console renderer
    │               │   └── JsonReportExporter.java      # Plain-text JSON serialization
    │               └── service/                         # High-level business orchestrator (DIP)
    │                   └── PortfolioService.java        # Core orchestration engine
    └── test/
        └── java/
            └── tmps/
                └── portfolio/
                    └── SolidPrinciplesTest.java         # Automated JUnit 5 test suite
```

---

## 3. SOLID Principles: In-Depth Implementation

### 3.1 Single Responsibility Principle (SRP)
> *"A class should have one, and only one, reason to change."*

In this engine, responsibilities are segregated into distinct modules:

| Responsibility | Owning Component | Reason to Change | What it does NOT do |
| :--- | :--- | :--- | :--- |
| **Domain State & Invariants** | `domain.*` (`StockAsset`, `P2PLoanAsset`, etc.) | Changes in asset definition or validation rules (e.g., negative shares forbidden). | Never calculates values, queries DBs, or prints strings. |
| **Valuation Math** | `strategy.*` (`StockValuationStrategy`, etc.) | Changes in financial pricing models (e.g. altering dividend formulas). | Never handles data persistence or presentation. |
| **Output Presentation** | `export.*` (`ConsoleReportExporter`, `JsonReportExporter`) | Changes in report formatting or serialization destinations. | Never touches domain business rules. |
| **Orchestration** | `service.PortfolioService` | Changes in high-level evaluation workflow. | Never hardcodes financial algorithms or storage engines. |

---

### 3.2 Open/Closed Principle (OCP)
> *"Software entities should be open for extension, but closed for modification."*

The valuation engine is designed so that new asset classes and novel pricing formulas can be added **without touching or recompiling existing service or strategy code**:

1. The `ValuationStrategy` interface defines a polymorphic contract: `supports(Asset)` and `evaluate(Asset)`.
2. The `DefaultValuationStrategyRegistry` maintains a dynamic registry of strategies.
3. **OCP Demonstration in Action (`Main.java` & `SolidPrinciplesTest.java`):**
   - The system starts with support for `StockAsset` and `P2PLoanAsset`.
   - When introducing `CryptoAsset` and `CryptoValuationStrategy`:
     - **0 lines** changed in `PortfolioService.java`
     - **0 lines** changed in `StockValuationStrategy.java`
     - **0 lines** changed in `P2PLoanValuationStrategy.java`
   - The new strategy is simply registered into the registry:
     ```java
     strategyRegistry.register(new CryptoValuationStrategy());
     ```
   - The engine immediately values cryptocurrencies seamlessly!

---

### 3.3 Dependency Inversion Principle (DIP)
> *"High-level modules should not depend on low-level modules. Both should depend on abstractions. Abstractions should not depend on details; details should depend on abstractions."*

`PortfolioService` represents the high-level business workflow. It adheres to DIP as follows:

1. **No concrete dependencies**: `PortfolioService` does **not** instantiate `new InMemoryAssetRepository()`, `new ConsoleReportExporter()`, or concrete strategies.
2. **Depends solely on Abstractions**:
   ```java
   public class PortfolioService {
       private final AssetRepository assetRepository;             // Abstraction
       private final ValuationStrategyProvider strategyProvider; // Abstraction
       private final ReportExporter reportExporter;              // Abstraction

       public PortfolioService(
               AssetRepository assetRepository,
               ValuationStrategyProvider strategyProvider,
               ReportExporter reportExporter
       ) {
           this.assetRepository = Objects.requireNonNull(assetRepository);
           this.strategyProvider = Objects.requireNonNull(strategyProvider);
           this.reportExporter = Objects.requireNonNull(reportExporter);
       }
       ...
   }
   ```
3. **Inversion of Control via Composition Root**:
   - `Main.java` serves as the manual **Composition Root** that instantiates implementations and injects them via constructor injection.
   - If we want to switch persistence from in-memory to PostgreSQL or switch output from the console to JSON, **`PortfolioService` remains completely unchanged**.

---

## 4. How to Compile and Run

### Option A: Using Maven (Recommended)
Compile and run the complete showcase application:
```bash
mvn clean compile exec:java
```

Execute automated JUnit 5 test suite:
```bash
mvn test
```

### Option B: Using Pure `javac` & `java` (Zero Tools)
Compile all Java sources into an output folder:
```bash
javac -d out $(find src/main/java -name "*.java")
```

Run the application:
```bash
java -cp out tmps.portfolio.Main
```

---

## 5. Lab Defense Guide (Academic Cheat Sheet)

When defending Lab 1 before your lab instructor or professor at UTM, focus on these key talking points:

### Defense Point 1: How did you implement SRP?
- **Answer:** *"In our domain models (like `StockAsset` and `P2PLoanAsset`), we only encapsulate state and validate basic invariants (e.g., verifying that shares > 0 and risk is within 0–100%). We deliberately excluded valuation algorithms and serialization logic from them. The valuation math is isolated in `ValuationStrategy` classes, and the rendering logic is encapsulated in `ReportExporter` classes. If the console table layout needs to change, only `ConsoleReportExporter` changes, without touching financial math or domain classes."*

### Defense Point 2: How did you prove OCP?
- **Answer:** *"We used the Strategy Pattern coupled with a dynamic strategy provider (`ValuationStrategyProvider`). In `Main.java` (and in our JUnit tests), we demonstrate that we can introduce a third asset class—`CryptoAsset` with `CryptoValuationStrategy`—without modifying a single existing line of code in `PortfolioService`, `StockValuationStrategy`, or `P2PLoanValuationStrategy`. The system is open for extension via new strategies, but closed for modification."*

### Defense Point 3: How does DIP differ from Dependency Injection (DI)?
- **Answer:** *"Dependency Injection is a mechanical technique (passing dependencies via constructor/setter), whereas DIP is an architectural principle stating that high-level policies must not depend on low-level details. In our code, `PortfolioService` (high-level) only knows about the interfaces `AssetRepository`, `ValuationStrategyProvider`, and `ReportExporter`. Low-level details like `InMemoryAssetRepository` or `ConsoleReportExporter` implement those interfaces. We then use Constructor Injection to fulfill the DIP requirement without any framework dependency."*

### Defense Point 4: Why are domain models implemented as Java Records?
- **Answer:** *"Java Records provide transparent, immutable data carriers with concise syntax. By enforcing validation in the record's compact constructor, we guarantee domain invariants without cluttering the model with boilerplate getters, `equals`, or mutable state, reinforcing SRP."*
