package tmps.portfolio.proxy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [PROXY - PROXY OBJECT]
 * Wraps the remote market data service, providing two critical structural capabilities:
 * 1. High-Performance In-Memory Caching (with configurable TTL) to prevent redundant network queries.
 * 2. Regulatory Compliance & Security Audit Logging (records every requester, symbol, timestamp, source).
 */
public class CachedAuditedMarketDataProxy implements MarketDataService {

    public record CacheRecord(BigDecimal price, long cachedAtMillis) {}

    public record AuditLogEntry(
            Instant timestamp,
            String requester,
            String symbol,
            BigDecimal price,
            String resolutionSource
    ) {}

    private final MarketDataService realService;
    private final long ttlMillis;

    private final Map<String, CacheRecord> cache = new ConcurrentHashMap<>();
    private final List<AuditLogEntry> auditTrail = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger cacheHitCount = new AtomicInteger(0);

    public CachedAuditedMarketDataProxy(MarketDataService realService, long ttlMillis) {
        this.realService = Objects.requireNonNull(realService, "realService cannot be null");
        this.ttlMillis = ttlMillis;
    }

    @Override
    public BigDecimal fetchLivePrice(String symbol, String requester) {
        String key = symbol.toUpperCase();
        long now = System.currentTimeMillis();

        CacheRecord existing = cache.get(key);
        BigDecimal resolvedPrice;
        String source;

        if (existing != null && (now - existing.cachedAtMillis()) <= ttlMillis) {
            // Cache Hit
            resolvedPrice = existing.price();
            source = "CACHE_HIT";
            cacheHitCount.incrementAndGet();
        } else {
            // Cache Miss -> Delegate to Real Subject
            resolvedPrice = realService.fetchLivePrice(key, requester);
            cache.put(key, new CacheRecord(resolvedPrice, now));
            source = "REMOTE_FETCH";
        }

        // Regulatory & Institutional Audit Trail
        auditTrail.add(new AuditLogEntry(
                Instant.now(),
                requester,
                key,
                resolvedPrice,
                source
        ));

        return resolvedPrice;
    }

    public int getCacheHitCount() {
        return cacheHitCount.get();
    }

    public List<AuditLogEntry> getAuditTrail() {
        return Collections.unmodifiableList(auditTrail);
    }

    public void clearCache() {
        cache.clear();
    }
}
