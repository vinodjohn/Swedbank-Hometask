package com.swedbank.swedbankhometask.unit.integration;

import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;
import com.swedbank.swedbankhometask.integration.configs.ExternalLoggingProperties;
import com.swedbank.swedbankhometask.integration.implementations.ExternalLoggingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalLoggingServiceImplTest {

    private static final String BASE_URL = "http://external-logging";

    private MockRestServiceServer server;
    private ExternalLoggingServiceImpl externalLoggingService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        externalLoggingService = new ExternalLoggingServiceImpl(
                builder.build(), new ExternalLoggingProperties(BASE_URL, 200, 3000));
    }

    @Test
    @DisplayName("log succeeds when the external system returns a success status")
    void logSucceedsOnSuccessStatus() {
        server.expect(requestTo(BASE_URL + "/200")).andRespond(withSuccess());

        assertThatCode(() -> externalLoggingService.log("debit 10 EUR")).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    @DisplayName("log throws when the external system returns an error status")
    void logThrowsOnErrorStatus() {
        server.expect(requestTo(BASE_URL + "/200")).andRespond(withServerError());

        assertThatThrownBy(() -> externalLoggingService.log("debit 10 EUR"))
                .isInstanceOf(ExternalLoggingException.class);
        server.verify();
    }
}
