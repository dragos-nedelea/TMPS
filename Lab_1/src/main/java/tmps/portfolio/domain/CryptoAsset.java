package tmps.portfolio.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * [SOLID: SRP - Single Responsibility Principle]
 * CryptoAsset is an additional asset type introduced to demonstrate the Open/Closed Principle (OCP).
 * Adding this new asset requires zero modifications to existing Stock or P2P logic.
 */
public record CryptoAsset(
        String id,
        String tokenSymbol,
        String networkName,
        BigDecimal unitsHeld,
        BigDecimal spotPriceUsd,
        BigDecimal estimatedGasFeeUsd
) implements Asset {

    public CryptoAsset {
        Objects.requireNonNull(id, "Asset ID cannot be null");
        Objects.requireNonNull(tokenSymbol, "Token symbol cannot be null");
        Objects.requireNonNull(networkName, "Network name cannot be null");
        Objects.requireNonNull(unitsHeld, "Units held cannot be null");
        Objects.requireNonNull(spotPriceUsd, "Spot price cannot be null");
        Objects.requireNonNull(estimatedGasFeeUsd, "Estimated gas fee cannot be null");

        if (unitsHeld.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Units held must be strictly positive, got: " + unitsHeld);
        }
        if (spotPriceUsd.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Spot price cannot be negative, got: " + spotPriceUsd);
        }
        if (estimatedGasFeeUsd.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Gas fee cannot be negative, got: " + estimatedGasFeeUsd);
        }
    }

    @Override
    public String name() {
        return tokenSymbol + " (" + networkName + " Network)";
    }

    @Override
    public String assetType() {
        return "CRYPTO_TOKEN";
    }
}
