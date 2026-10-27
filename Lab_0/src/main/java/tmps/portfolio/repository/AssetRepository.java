package tmps.portfolio.repository;

import tmps.portfolio.domain.Asset;

import java.util.List;
import java.util.Optional;

/**
 * [SOLID: DIP - Dependency Inversion Principle]
 * AssetRepository is the high-level persistence abstraction.
 * The core valuation service depends on this interface, NEVER on a concrete database,
 * file system, or in-memory map.
 */
public interface AssetRepository {

    void save(Asset asset);

    Optional<Asset> findById(String id);

    List<Asset> findAll();

    void deleteById(String id);

    int count();
}
