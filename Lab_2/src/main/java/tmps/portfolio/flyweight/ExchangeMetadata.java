package tmps.portfolio.flyweight;

import java.util.Objects;

/**
 * [FLYWEIGHT]
 * Represents the immutable intrinsic state of a financial market exchange.
 * Shared across thousands of individual asset positions to minimize memory footprint.
 */
public final class ExchangeMetadata {

    private final String micCode;          // Market Identifier Code (e.g. XNYS, XNAS, XLON)
    private final String exchangeName;     // e.g. New York Stock Exchange
    private final String country;          // e.g. United States
    private final String timezone;         // e.g. America/New_York
    private final String baseCurrency;     // e.g. USD
    private final String regulator;        // e.g. SEC / FINRA

    public ExchangeMetadata(
            String micCode,
            String exchangeName,
            String country,
            String timezone,
            String baseCurrency,
            String regulator
    ) {
        this.micCode = Objects.requireNonNull(micCode, "micCode cannot be null");
        this.exchangeName = Objects.requireNonNull(exchangeName, "exchangeName cannot be null");
        this.country = Objects.requireNonNull(country, "country cannot be null");
        this.timezone = Objects.requireNonNull(timezone, "timezone cannot be null");
        this.baseCurrency = Objects.requireNonNull(baseCurrency, "baseCurrency cannot be null");
        this.regulator = Objects.requireNonNull(regulator, "regulator cannot be null");
    }

    public String getMicCode() {
        return micCode;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getCountry() {
        return country;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getRegulator() {
        return regulator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeMetadata that)) return false;
        return Objects.equals(micCode, that.micCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(micCode);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, %s - Reg: %s)", micCode, exchangeName, country, baseCurrency, regulator);
    }
}
