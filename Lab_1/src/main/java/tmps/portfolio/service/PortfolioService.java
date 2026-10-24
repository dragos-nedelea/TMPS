package tmps.portfolio.service;

import tmps.portfolio.domain.Asset;
import tmps.portfolio.domain.PortfolioReport;
import tmps.portfolio.domain.ValuationResult;
import tmps.portfolio.export.ReportExporter;
import tmps.portfolio.repository.AssetRepository;
import tmps.portfolio.strategy.ValuationStrategy;
import tmps.portfolio.strategy.ValuationStrategyProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * [SOLID: DIP & SRP]
 * PortfolioService is the high-level application orchestrator.
 *
 * 1. DIP Compliance:
 *    - Notice that PortfolioService does NOT instantiate any 'new InMemoryAssetRepository()',
 *      'new ConsoleReportExporter()', or any concrete Strategy.
 *    - It depends strictly on ABSTRACTIONS:
 *        * AssetRepository
 *        * ValuationStrategyProvider
 *        * ReportExporter
 *    - All dependencies are provided from the outside via CONSTRUCTOR INJECTION.
 *
 * 2. SRP Compliance:
 *    - Its single responsibility is orchestrating the valuation pipeline.
 *    - It does not store records (delegated to AssetRepository).
 *    - It does not contain financial formulas (delegated to ValuationStrategy instances).
 *    - It does not render or print reports (delegated to ReportExporter).
 */
public class PortfolioService {

    private final AssetRepository assetRepository;
    private final ValuationStrategyProvider strategyProvider;
    private final ReportExporter reportExporter;

    /**
     * [DIP: Constructor Injection]
     * Inversion of Control: The orchestrator's dependencies are injected by the caller.
     */
    public PortfolioService(
            AssetRepository assetRepository,
            ValuationStrategyProvider strategyProvider,
            ReportExporter reportExporter
    ) {
        this.assetRepository = Objects.requireNonNull(assetRepository, "AssetRepository cannot be null");
        this.strategyProvider = Objects.requireNonNull(strategyProvider, "ValuationStrategyProvider cannot be null");
        this.reportExporter = Objects.requireNonNull(reportExporter, "ReportExporter cannot be null");
    }

    /**
     * Performs valuation of all assets registered in the repository.
     *
     * @param portfolioId Unique identifier for this portfolio
     * @param investorName Name of the portfolio owner
     * @return Immutable PortfolioReport domain object
     */
    public PortfolioReport evaluatePortfolio(String portfolioId, String investorName) {
        List<Asset> assets = assetRepository.findAll();
        List<ValuationResult> results = new ArrayList<>(assets.size());
        BigDecimal totalValuation = BigDecimal.ZERO;

        for (Asset asset : assets) {
            // [OCP & DIP] The service requests the appropriate strategy abstraction dynamically
            ValuationStrategy strategy = strategyProvider.findStrategy(asset);
            ValuationResult result = strategy.evaluate(asset);
            results.add(result);
            totalValuation = totalValuation.add(result.fairValue());
        }

        return new PortfolioReport(
                portfolioId,
                investorName,
                results,
                totalValuation,
                Instant.now()
        );
    }

    /**
     * Orchestrates evaluation and dispatches the result to the configured ReportExporter.
     *
     * @param portfolioId Unique identifier for this portfolio
     * @param investorName Name of the portfolio owner
     * @return The evaluated PortfolioReport
     */
    public PortfolioReport evaluateAndExport(String portfolioId, String investorName) {
        PortfolioReport report = evaluatePortfolio(portfolioId, investorName);
        // [SRP & DIP] High-level service delegates rendering to the exporter abstraction
        reportExporter.export(report);
        return report;
    }
}
