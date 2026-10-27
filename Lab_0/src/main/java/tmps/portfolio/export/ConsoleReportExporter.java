package tmps.portfolio.export;

import tmps.portfolio.domain.PortfolioReport;
import tmps.portfolio.domain.ValuationResult;

import java.io.PrintStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * [SOLID: SRP]
 * ConsoleReportExporter is solely responsible for rendering a PortfolioReport
 * to standard output (or any specified PrintStream) with clean, readable formatting.
 */
public class ConsoleReportExporter implements ReportExporter {

    private final PrintStream out;
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    public ConsoleReportExporter() {
        this(System.out);
    }

    public ConsoleReportExporter(PrintStream out) {
        this.out = Objects.requireNonNull(out, "PrintStream cannot be null");
    }

    @Override
    public void export(PortfolioReport report) {
        Objects.requireNonNull(report, "PortfolioReport cannot be null");

        String horizontalLine = "=".repeat(90);
        String subLine = "-".repeat(90);

        out.println();
        out.println(horizontalLine);
        out.printf("  INVESTMENT PORTFOLIO VALUATION REPORT%n");
        out.printf("  Portfolio ID: %-25s Investor: %s%n", report.portfolioId(), report.investorName());
        out.printf("  Valuation Timestamp: %s%n", TIME_FORMATTER.format(report.evaluatedAt()));
        out.println(horizontalLine);

        out.printf("%-10s | %-28s | %-14s | %-12s | %s%n",
                "ASSET ID", "ASSET NAME", "TYPE", "FAIR VALUE", "STRATEGY & RATIONALE");
        out.println(subLine);

        for (ValuationResult item : report.items()) {
            out.printf("%-10s | %-28s | %-14s | $%11s | [%s]%n",
                    item.asset().id(),
                    truncate(item.asset().name(), 28),
                    item.asset().assetType(),
                    item.fairValue().toPlainString(),
                    item.valuationStrategyName());
            out.printf("%10s   %-28s   %-14s   %12s   Details: %s%n",
                    "", "", "", "", item.calculationSummary());
            out.println(subLine);
        }

        out.println(horizontalLine);
        out.printf("  TOTAL CONSOLIDATED PORTFOLIO VALUE: $%s%n", report.totalValuation().toPlainString());
        out.printf("  TOTAL ASSETS APPRAISED: %d%n", report.items().size());
        out.println(horizontalLine);
        out.println();
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
