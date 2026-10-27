package tmps.portfolio.composite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * [COMPOSITE - COMPOSITE CONTAINER]
 * Represents a composite container such as an institutional Fund of Funds (FoF),
 * sector portfolio (e.g. Tech, ESG, Fixed Income), or regional asset basket.
 * Treats nested portfolios and individual asset leaves uniformly.
 */
public class PortfolioComposite implements PortfolioComponent {

    private final String id;
    private final String name;
    private final String currency;
    private final List<PortfolioComponent> children = new ArrayList<>();

    public PortfolioComposite(String id, String name, String currency) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.currency = Objects.requireNonNull(currency, "currency cannot be null");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public void add(PortfolioComponent component) {
        Objects.requireNonNull(component, "child component cannot be null");
        children.add(component);
    }

    @Override
    public void remove(PortfolioComponent component) {
        children.remove(component);
    }

    @Override
    public List<PortfolioComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public BigDecimal calculateFairValue() {
        BigDecimal total = BigDecimal.ZERO;
        for (PortfolioComponent child : children) {
            total = total.add(child.calculateFairValue());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int getAssetCount() {
        int count = 0;
        for (PortfolioComponent child : children) {
            count += child.getAssetCount();
        }
        return count;
    }

    @Override
    public void printHierarchy(String indent, StringBuilder out) {
        out.append(indent)
                .append("📁 [COMPOSITE PORTFOLIO] ")
                .append(name)
                .append(" (ID: ")
                .append(id)
                .append(") | Total Value: ")
                .append(calculateFairValue())
                .append(" ")
                .append(currency)
                .append(" | Assets: ")
                .append(getAssetCount())
                .append("\n");

        for (PortfolioComponent child : children) {
            child.printHierarchy(indent + "    ", out);
        }
    }

    @Override
    public String toString() {
        return "PortfolioComposite{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", totalValue=" + calculateFairValue() +
                ", childrenCount=" + children.size() +
                '}';
    }
}
