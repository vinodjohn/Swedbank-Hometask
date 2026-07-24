package com.swedbank.swedbankhometask.integration.integration;

import com.swedbank.swedbankhometask.integration.api.ExternalLoggingService;
import com.swedbank.swedbankhometask.integration.configs.ExternalLoggingProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
class ExternalLoggingWiringTest {

    @Autowired
    private ExternalLoggingService externalLoggingService;

    @Autowired
    private ExternalLoggingProperties externalLoggingProperties;

    @Test
    @DisplayName("binds the external logging properties from configuration")
    void bindsExternalLoggingProperties() {
        assertThat(externalLoggingProperties.baseUrl()).isEqualTo("https://httpstat.us");
        assertThat(externalLoggingProperties.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("exposes the external logging service as a bean")
    void exposesExternalLoggingServiceBean() {
        assertThat(externalLoggingService).isNotNull();
    }
}
