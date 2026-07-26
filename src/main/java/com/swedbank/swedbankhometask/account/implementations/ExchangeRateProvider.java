package com.swedbank.swedbankhometask.account.implementations;

import com.swedbank.swedbankhometask.account.configs.ExchangeProperties;
import com.swedbank.swedbankhometask.account.models.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;

/**
 * Converts amounts between currencies using the configured fixed rates.
 *
 * @author vinodjohn
 * @since 25.07.2026
 */
@Component
@EnableConfigurationProperties(ExchangeProperties.class)
@RequiredArgsConstructor
public class ExchangeRateProvider {
    private static final int SCALE = 4;

    private final ExchangeProperties properties;

    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        return amount.multiply(rateOf(to)).divide(rateOf(from), SCALE, RoundingMode.HALF_UP);
    }

    // PRIVATE METHODS //
    private BigDecimal rateOf(Currency currency) {
        BigDecimal rate = properties.rates().get(currency);
        if (rate == null) {
            throw new IllegalStateException(MessageFormat.format("No exchange rate configured for {0}", currency));
        }
        return rate;
    }
}
