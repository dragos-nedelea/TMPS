package tmps.portfolio.bridge.report;

import tmps.portfolio.bridge.renderer.ReportRenderer;
import tmps.portfolio.composite.PortfolioComponent;

import java.util.Objects;

/**
 * [BRIDGE - ABSTRACTION]
 * High-level report abstraction that delegates low-level formatting
 * to the injected ReportRenderer implementor.
 */
public abstract class PortfolioReport {

    protected final ReportRenderer renderer;

    public PortfolioReport(ReportRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer cannot be null");
    }

    public abstract String generate(PortfolioComponent component);
}
