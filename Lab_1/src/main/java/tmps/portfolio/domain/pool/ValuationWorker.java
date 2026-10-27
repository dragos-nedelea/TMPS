package tmps.portfolio.domain.pool;

import tmps.portfolio.domain.models.Asset;
import tmps.portfolio.domain.models.Portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reusable worker object managed by the Object Pool.
 * Holds pre-allocated calculation buffers to avoid GC pressure during high-throughput analysis.
 */
public class ValuationWorker {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private final int workerId;
    private final long[] calculationBuffer; // Simulates pre-allocated memory buffer
    private int processedJobCount = 0;
    private boolean inUse = false;

    public ValuationWorker() {
        this.workerId = ID_GENERATOR.getAndIncrement();
        this.calculationBuffer = new long[1024]; // 1K elements allocated once
    }

    public int getWorkerId() {
        return workerId;
    }

    public int getProcessedJobCount() {
        return processedJobCount;
    }

    boolean isInUse() {
        return inUse;
    }

    void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    /**
     * Executes valuation processing using the worker's internal state.
     */
    public ValuationReport executeValuation(Portfolio portfolio) {
        if (!inUse) {
            throw new IllegalStateException("Worker " + workerId + " is not checked out from the pool!");
        }

        processedJobCount++;
        BigDecimal totalAssetValue = BigDecimal.ZERO;
        for (Asset asset : portfolio.getAssets()) {
            totalAssetValue = totalAssetValue.add(asset.calculateFairValue());
        }

        BigDecimal netWorth = totalAssetValue.add(portfolio.getCashBalance());
        double allocationEquity = 0.0;
        double allocationCrypto = 0.0;
        double allocationDebt = 0.0;

        if (totalAssetValue.compareTo(BigDecimal.ZERO) > 0) {
            for (Asset a : portfolio.getAssets()) {
                double pct = a.calculateFairValue().divide(totalAssetValue, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                switch (a.getType()) {
                    case PUBLIC_EQUITY -> allocationEquity += pct;
                    case CRYPTO_TOKEN -> allocationCrypto += pct;
                    case P2P_DEBT -> allocationDebt += pct;
                    default -> {}
                }
            }
        }

        return new ValuationReport(
                workerId,
                portfolio.getId(),
                portfolio.getInvestorName(),
                portfolio.getAssets().size(),
                totalAssetValue,
                portfolio.getCashBalance(),
                netWorth,
                allocationEquity,
                allocationDebt,
                allocationCrypto
        );
    }

    /**
     * Resets worker state before returning to the pool.
     */
    public void reset() {
        // Clear calculation buffers for clean reuse
        for (int i = 0; i < Math.min(calculationBuffer.length, 64); i++) {
            calculationBuffer[i] = 0;
        }
    }

    public record ValuationReport(
            int workerId,
            String portfolioId,
            String investor,
            int assetCount,
            BigDecimal assetValuation,
            BigDecimal cashBalance,
            BigDecimal netWorth,
            double equityPct,
            double debtPct,
            double cryptoPct
    ) {}
}
