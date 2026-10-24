package tmps.portfolio.domain;

/**
 * [SOLID: SRP - Single Responsibility Principle]
 * The Asset interface defines only the essential domain identification and metadata.
 * It contains NO valuation logic, NO persistence logic, and NO formatting logic.
 */
public interface Asset {
    String id();
    String name();
    String assetType();
}
