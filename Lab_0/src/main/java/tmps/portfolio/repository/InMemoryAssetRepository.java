package tmps.portfolio.repository;

import tmps.portfolio.domain.Asset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * [SOLID: DIP & SRP]
 * Low-level implementation of AssetRepository storing records in memory.
 * Can be swapped for a SqlAssetRepository or MongoAssetRepository at any time
 * without changing a single line of PortfolioService code.
 */
public class InMemoryAssetRepository implements AssetRepository {

    private final Map<String, Asset> storage = new LinkedHashMap<>();

    @Override
    public synchronized void save(Asset asset) {
        Objects.requireNonNull(asset, "Asset cannot be null");
        storage.put(asset.id(), asset);
    }

    @Override
    public synchronized Optional<Asset> findById(String id) {
        Objects.requireNonNull(id, "Asset ID cannot be null");
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<Asset> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    @Override
    public synchronized void deleteById(String id) {
        Objects.requireNonNull(id, "Asset ID cannot be null");
        storage.remove(id);
    }

    @Override
    public synchronized int count() {
        return storage.size();
    }
}
