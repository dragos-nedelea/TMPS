package tmps.portfolio.proxy;

import tmps.portfolio.adapter.PriceFeedProvider;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [PROXY - REAL SUBJECT]
 * Represents the heavyweight, network-intensive external market data connection.
 * Tracks network invocation counts.
 */
public class RemoteMarketDataService implements MarketDataService {

    private final PriceFeedProvider feedProvider;
    private final AtomicInteger networkCallCount = new AtomicInteger(0);

    public RemoteMarketDataService(PriceFeedProvider feedProvider) {
        this.feedProvider = Objects.requireNonNull(feedProvider, "feedProvider cannot be null");
    }

    @Override
    public BigDecimal fetchLivePrice(String symbol, String requester) {
        networkCallCount.incrementAndGet();

        // Simulate lightweight network dispatch latency in production
        try {
            Thread.sleep(15);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        return feedProvider.getLatestPrice(symbol);
    }

    public int getNetworkCallCount() {
        return networkCallCount.get();
    }
}
