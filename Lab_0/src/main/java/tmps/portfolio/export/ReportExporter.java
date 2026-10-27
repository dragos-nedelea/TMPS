package tmps.portfolio.export;

import tmps.portfolio.domain.PortfolioReport;

/**
 * [SOLID: DIP & SRP]
 * ReportExporter defines the contract for serializing or displaying valuation reports.
 * - SRP: Isolates output formatting (Console, JSON, CSV) from valuation math and domain models.
 * - DIP: High-level services depend on this interface, allowing formatting destinations to be swapped.
 */
public interface ReportExporter {

    /**
     * Formats and emits the generated portfolio valuation report.
     *
     * @param report The completed valuation report
     */
    void export(PortfolioReport report);
}
