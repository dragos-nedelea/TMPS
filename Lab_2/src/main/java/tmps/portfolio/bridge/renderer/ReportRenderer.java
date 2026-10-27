package tmps.portfolio.bridge.renderer;

import java.math.BigDecimal;

/**
 * [BRIDGE - IMPLEMENTOR INTERFACE]
 * Decouples the rendering channel and presentation format from the report type and business logic.
 */
public interface ReportRenderer {

    String renderHeader(String reportTitle, String subtitle);

    String renderSummary(String id, String name, BigDecimal totalValue, int totalAssets);

    String renderHierarchy(String formattedHierarchy);

    String renderFooter();
}
