package tmps.portfolio.bridge.renderer;

import java.math.BigDecimal;

/**
 * [BRIDGE - CONCRETE IMPLEMENTOR 1]
 * Formats report output using structured ASCII / Unicode console banners and tables.
 */
public class ConsoleTabularRenderer implements ReportRenderer {

    @Override
    public String renderHeader(String reportTitle, String subtitle) {
        String border = "═".repeat(88);
        return "\n" + border + "\n" +
                "  📊 " + reportTitle.toUpperCase() + "\n" +
                "  " + subtitle + "\n" +
                border + "\n";
    }

    @Override
    public String renderSummary(String id, String name, BigDecimal totalValue, int totalAssets) {
        return "  ► Component ID   : " + id + "\n" +
                "  ► Portfolio Name : " + name + "\n" +
                "  ► Asset Count    : " + totalAssets + " positions\n" +
                "  ► Net Fair Value : $" + totalValue + "\n";
    }

    @Override
    public String renderHierarchy(String formattedHierarchy) {
        return "  ── Hierarchy Breakdown ─────────────────────────────────────────────────────────\n" +
                formattedHierarchy;
    }

    @Override
    public String renderFooter() {
        return "═".repeat(88) + "\n";
    }
}
