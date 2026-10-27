package tmps.portfolio.domain.models;

import java.math.BigDecimal;

/**
 * Base contract for financial assets.
 * Implements Cloneable to support the Prototype creational pattern.
 */
public interface Asset extends Cloneable {

    /**
     * Unique identifier of the asset instance (e.g. STK-001, CRP-101).
     */
    String getId();

    /**
     * Human-readable label or ticker of the asset.
     */
    String getName();

    /**
     * Financial asset category.
     */
    AssetType getType();

    /**
     * Currency code denomination (e.g. USD, EUR).
     */
    String getCurrency();

    /**
     * Calculates the estimated fair market value.
     */
    BigDecimal calculateFairValue();

    /**
     * Prototype Pattern: Deep copy cloning operation.
     *
     * @return a distinct cloned instance of the asset.
     */
    Asset cloneAsset();
}
