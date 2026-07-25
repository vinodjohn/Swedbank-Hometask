package com.swedbank.swedbankhometask.account.configs;

import com.swedbank.swedbankhometask.account.models.Currency;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Fixed exchange rates, expressed as units of the currency per one EUR.
 *
 * @author vinodjohn
 * @since 25.07.2026
 */
@ConfigurationProperties(prefix = "exchange")
public record ExchangeProperties(Map<Currency, BigDecimal> rates) {
}
