package tmps.portfolio.composite;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * [COMPOSITE - COMPONENT]
 * Common abstract contract representing any financial asset, sub-portfolio,
 * or asset bundle in the institutional investment hierarchy.
 */
public interface PortfolioComponent {

    String getId();

    String getName();

    /**
     * Recursively calculates the total fair market value of this component.
     */
    BigDecimal calculateFairValue();

    /**
     * Recursively computes the total number of individual asset positions held.
     */
    int getAssetCount();

    /**
     * Generates a readable indented hierarchy visualization.
     */
    void printHierarchy(String indent, StringBuilder out);

    default void add(PortfolioComponent component) {
        throw new UnsupportedOperationException("Cannot add child component to a Leaf node: " + getId());
    }

    default void remove(PortfolioComponent component) {
        throw new UnsupportedOperationException("Cannot remove child component from a Leaf node: " + getId());
    }

    default List<PortfolioComponent> getChildren() {
        return Collections.emptyList();
    }

    default boolean isComposite() {
        return false;
    }
}
