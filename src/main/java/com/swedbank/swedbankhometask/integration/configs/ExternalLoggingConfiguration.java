package com.swedbank.swedbankhometask.integration.configs;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Wires the {@link RestClient} used to call the external logging system.
 *
 * @author vinodjohn
 * @since 24.07.2026
 */
@Configuration
@EnableConfigurationProperties(ExternalLoggingProperties.class)
public class ExternalLoggingConfiguration {
    @Bean
    public RestClient externalLoggingRestClient(ExternalLoggingProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.timeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.timeoutMs()));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }
}
