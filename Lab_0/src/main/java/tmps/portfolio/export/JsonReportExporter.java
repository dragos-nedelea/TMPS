package tmps.portfolio.export;

import tmps.portfolio.domain.PortfolioReport;
import tmps.portfolio.domain.ValuationResult;

import java.io.PrintStream;
import java.util.Objects;

/**
 * [SOLID: SRP & DIP]
 * Pure Java JSON report exporter (zero external JSON library dependencies).
 * Illustrates that swapping output mediums requires zero changes to core business logic.
 */
public class JsonReportExporter implements ReportExporter {

    private final PrintStream out;

    public JsonReportExporter() {
        this(System.out);
    }

    public JsonReportExporter(PrintStream out) {
        this.out = Objects.requireNonNull(out, "PrintStream cannot be null");
    }

    @Override
    public void export(PortfolioReport report) {
        Objects.requireNonNull(report, "PortfolioReport cannot be null");

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"portfolioId\": \"").append(escape(report.portfolioId())).append("\",\n");
        json.append("  \"investorName\": \"").append(escape(report.investorName())).append("\",\n");
        json.append("  \"evaluatedAt\": \"").append(report.evaluatedAt().toString()).append("\",\n");
        json.append("  \"totalValuation\": ").append(report.totalValuation().toPlainString()).append(",\n");
        json.append("  \"itemCount\": ").append(report.items().size()).append(",\n");
        json.append("  \"items\": [\n");

        for (int i = 0; i < report.items().size(); i++) {
            ValuationResult item = report.items().get(i);
            json.append("    {\n");
            json.append("      \"assetId\": \"").append(escape(item.asset().id())).append("\",\n");
            json.append("      \"name\": \"").append(escape(item.asset().name())).append("\",\n");
            json.append("      \"assetType\": \"").append(escape(item.asset().assetType())).append("\",\n");
            json.append("      \"fairValue\": ").append(item.fairValue().toPlainString()).append(",\n");
            json.append("      \"strategy\": \"").append(escape(item.valuationStrategyName())).append("\",\n");
            json.append("      \"details\": \"").append(escape(item.calculationSummary())).append("\"\n");
            json.append("    }").append(i < report.items().size() - 1 ? "," : "").append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        out.println(json);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
