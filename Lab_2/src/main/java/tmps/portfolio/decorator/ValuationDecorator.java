package tmps.portfolio.decorator;

import tmps.portfolio.composite.PortfolioComponent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * [DECORATOR - BASE DECORATOR]
 * Abstract base decorator that implements PortfolioComponent while maintaining
 * a reference to the wrapped PortfolioComponent.
 * Subclasses dynamically attach financial adjustments (taxes, management fees, hedging).
 */
public abstract class ValuationDecorator implements PortfolioComponent {

    protected final PortfolioComponent decoratedComponent;

    public ValuationDecorator(PortfolioComponent decoratedComponent) {
        this.decoratedComponent = Objects.requireNonNull(decoratedComponent, "decoratedComponent cannot be null");
    }

    @Override
    public String getId() {
        return decoratedComponent.getId();
    }

    @Override
    public String getName() {
        return decoratedComponent.getName();
    }

    @Override
    public int getAssetCount() {
        return decoratedComponent.getAssetCount();
    }

    @Override
    public List<PortfolioComponent> getChildren() {
        return decoratedComponent.getChildren();
    }

    @Override
    public boolean isComposite() {
        return decoratedComponent.isComposite();
    }

    @Override
    public BigDecimal calculateFairValue() {
        return decoratedComponent.calculateFairValue();
    }

    @Override
    public void printHierarchy(String indent, StringBuilder out) {
        decoratedComponent.printHierarchy(indent, out);
    }
}
