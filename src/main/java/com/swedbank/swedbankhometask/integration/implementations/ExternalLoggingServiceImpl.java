package com.swedbank.swedbankhometask.integration.implementations;

import com.swedbank.swedbankhometask.integration.api.ExternalLoggingService;
import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;
import com.swedbank.swedbankhometask.integration.configs.ExternalLoggingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementation of {@link ExternalLoggingService} backed by a REST call.
 *
 * @author vinodjohn
 * @since 24.07.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalLoggingServiceImpl implements ExternalLoggingService {
    private final RestClient externalLoggingRestClient;
    private final ExternalLoggingProperties properties;

    @Override
    public void log(String message) throws ExternalLoggingException {
        try {
            externalLoggingRestClient.get()
                    .uri("/{status}", properties.statusCode())
                    .retrieve()
                    .toBodilessEntity();
            log.info("External logging call succeeded for: {}", message);
        } catch (RestClientException ex) {
            throw new ExternalLoggingException(message, ex);
        }
    }
}
