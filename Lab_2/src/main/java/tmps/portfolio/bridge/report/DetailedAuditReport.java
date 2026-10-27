package tmps.portfolio.bridge.report;

import tmps.portfolio.bridge.renderer.ReportRenderer;
import tmps.portfolio.composite.PortfolioComponent;

/**
 * [BRIDGE - REFINED ABSTRACTION 2]
 * Full regulatory audit and risk breakdown report.
 * Renders complete nested hierarchies and itemized asset valuations.
 */
public class DetailedAuditReport extends PortfolioReport {

    public DetailedAuditReport(ReportRenderer renderer) {
        super(renderer);
    }

    @Override
    public String generate(PortfolioComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append(renderer.renderHeader("Comprehensive Audit & Risk Valuation", "Itemized Position Breakdown & Regulatory Verification"));
        sb.append(renderer.renderSummary(
                component.getId(),
                component.getName(),
                component.calculateFairValue(),
                component.getAssetCount()
        ));

        StringBuilder hierarchyBuilder = new StringBuilder();
        component.printHierarchy("    ", hierarchyBuilder);
        sb.append(renderer.renderHierarchy(hierarchyBuilder.toString()));

        sb.append(renderer.renderFooter());
        return sb.toString();
    }
}
