package com.swedbank.swedbankhometask.integration.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the external logging call.
 *
 * @author vinodjohn
 * @since 24.07.2026
 */
@ConfigurationProperties(prefix = "external.logging")
public record ExternalLoggingProperties(
        @DefaultValue("https://mock.httpstatus.io") String baseUrl,
        @DefaultValue("200") int statusCode,
        @DefaultValue("3000") int timeoutMs) {
}
