package tmps.portfolio.bridge.renderer;

import java.math.BigDecimal;

/**
 * [BRIDGE - CONCRETE IMPLEMENTOR 2]
 * Serializes financial reports into structured JSON payloads for API or microservice transmission.
 */
public class JsonReportRenderer implements ReportRenderer {

    @Override
    public String renderHeader(String reportTitle, String subtitle) {
        return "{\n" +
                "  \"reportType\": \"" + escape(reportTitle) + "\",\n" +
                "  \"description\": \"" + escape(subtitle) + "\",\n";
    }

    @Override
    public String renderSummary(String id, String name, BigDecimal totalValue, int totalAssets) {
        return "  \"portfolio\": {\n" +
                "    \"id\": \"" + escape(id) + "\",\n" +
                "    \"name\": \"" + escape(name) + "\",\n" +
                "    \"assetCount\": " + totalAssets + ",\n" +
                "    \"fairValue\": " + totalValue + "\n" +
                "  },\n";
    }

    @Override
    public String renderHierarchy(String formattedHierarchy) {
        // Escapes tree newlines into json string or compact payload
        return "  \"rawHierarchy\": \"" + escape(formattedHierarchy.replace("\n", "\\n").replace("\"", "\\\"")) + "\",\n";
    }

    @Override
    public String renderFooter() {
        return "  \"status\": \"SUCCESS\"\n}";
    }

    private String escape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
