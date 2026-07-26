package com.swedbank.swedbankhometask.unit.account;

import com.swedbank.swedbankhometask.account.configs.ExchangeProperties;
import com.swedbank.swedbankhometask.account.implementations.ExchangeRateProvider;
import com.swedbank.swedbankhometask.account.models.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateProviderTest {
    private final ExchangeRateProvider provider = new ExchangeRateProvider(new ExchangeProperties(rates()));

    @Test
    @DisplayName("converts from EUR to another currency using its rate")
    void convertsFromEur() {
        BigDecimal result = provider.convert(Currency.EUR, Currency.USD, new BigDecimal("100.00"));

        assertThat(result).isEqualByComparingTo("109.0000");
    }

    @Test
    @DisplayName("converts between two non-EUR currencies through their rates")
    void convertsBetweenNonEur() {
        BigDecimal result = provider.convert(Currency.USD, Currency.SEK, new BigDecimal("50.00"));

        assertThat(result).isEqualByComparingTo("520.6422");
    }

    @Test
    @DisplayName("rounds the converted amount half up to four decimals")
    void roundsHalfUp() {
        BigDecimal result = provider.convert(Currency.EUR, Currency.GBP, new BigDecimal("1.00"));

        assertThat(result.scale()).isEqualTo(4);
        assertThat(result).isEqualByComparingTo("0.8500");
    }

    @Test
    @DisplayName("throws when no rate is configured for a currency")
    void throwsWhenRateMissing() {
        ExchangeRateProvider incomplete = new ExchangeRateProvider(
                new ExchangeProperties(Map.of(Currency.EUR, BigDecimal.ONE)));

        assertThatThrownBy(() -> incomplete.convert(Currency.EUR, Currency.USD, BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class);
    }

    // PRIVATE METHODS //
    private Map<Currency, BigDecimal> rates() {
        Map<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
        rates.put(Currency.EUR, new BigDecimal("1.0"));
        rates.put(Currency.USD, new BigDecimal("1.09"));
        rates.put(Currency.SEK, new BigDecimal("11.35"));
        rates.put(Currency.GBP, new BigDecimal("0.85"));
        return rates;
    }
}
