package tmps.portfolio.bridge.report;

import tmps.portfolio.bridge.renderer.ReportRenderer;
import tmps.portfolio.composite.PortfolioComponent;

/**
 * [BRIDGE - REFINED ABSTRACTION 1]
 * Concise executive report for C-suite and investment committees.
 * Omits detailed asset-level tree lines in favor of high-level valuations.
 */
public class ExecutiveSummaryReport extends PortfolioReport {

    public ExecutiveSummaryReport(ReportRenderer renderer) {
        super(renderer);
    }

    @Override
    public String generate(PortfolioComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append(renderer.renderHeader("Executive Portfolio Summary", "Institutional High-Level Capital Overview"));
        sb.append(renderer.renderSummary(
                component.getId(),
                component.getName(),
                component.calculateFairValue(),
                component.getAssetCount()
        ));
        sb.append(renderer.renderFooter());
        return sb.toString();
    }
}
