package com.swedbank.swedbankhometask.e2e.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AccountLifecycleE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("opens an account and reads back four zero balances")
    void opensAccountAndReadsBalances() {
        ResponseEntity<JsonNode> created = restTemplate.postForEntity(
                "/accounts", Map.of("owner", "Alice"), JsonNode.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().path("success").asBoolean()).isTrue();

        String id = created.getBody().path("data").path("id").asText();
        assertThat(id).isNotBlank();

        ResponseEntity<JsonNode> balance = restTemplate.getForEntity(
                "/accounts/{id}/balance", JsonNode.class, id);

        assertThat(balance.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode balances = balance.getBody().path("data").path("balances");
        assertThat(balances).hasSize(4);
        balances.forEach(node ->
                assertThat(new BigDecimal(node.path("amount").asText())).isEqualByComparingTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("returns 404 when reading the balance of an unknown account")
    void returnsNotFoundForUnknownAccount() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/accounts/{id}/balance", JsonNode.class, UUID.randomUUID());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().path("success").asBoolean()).isFalse();
    }
}
