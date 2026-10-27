package tmps.portfolio.domain.factory;

import tmps.portfolio.domain.models.Asset;

/**
 * Factory Method Pattern: Abstract Creator.
 *
 * Defines the factory method contract for instantiating specialized financial assets
 * while providing common template/auditing hooks.
 */
public abstract class AssetFactory<T extends Asset> {

    /**
     * The Factory Method to be implemented by concrete creator subclasses.
     */
    public abstract T createAsset(String id, String symbolOrRef, double quantityOrPrincipal, double unitPriceOrRate);

    /**
     * Common template processing: creates the asset and performs compliance logging.
     */
    public T issueAsset(String id, String symbolOrRef, double quantityOrPrincipal, double unitPriceOrRate) {
        T asset = createAsset(id, symbolOrRef, quantityOrPrincipal, unitPriceOrRate);
        System.out.printf("  [AssetFactory] Issued %s [%s] with fair value: %s %s%n",
                asset.getType(), asset.getName(), asset.calculateFairValue(), asset.getCurrency());
        return asset;
    }
}
