package com.multibank.integration;

import com.multibank.config.BankIntegrationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractRestBankClient implements BankApiClient {

    private final RestTemplate restTemplate;
    private final BankIntegrationProperties.BankProperties properties;

    protected AbstractRestBankClient(RestTemplate restTemplate, BankIntegrationProperties.BankProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public List<ExternalBankAccount> fetchAccounts() {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            return stubAccounts();
        }
        ResponseEntity<ExternalBankAccount[]> response = restTemplate.exchange(
                properties.getBaseUrl() + "/accounts",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                ExternalBankAccount[].class
        );
        ExternalBankAccount[] body = response.getBody();
        return body == null ? Collections.emptyList() : Arrays.asList(body);
    }

    @Override
    public List<ExternalTransaction> fetchTransactions(String accountId) {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            return stubTransactions(accountId);
        }
        ResponseEntity<ExternalTransaction[]> response = restTemplate.exchange(
                properties.getBaseUrl() + "/accounts/" + accountId + "/transactions",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                ExternalTransaction[].class
        );
        ExternalTransaction[] body = response.getBody();
        return body == null ? Collections.emptyList() : Arrays.asList(body);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(properties.getApiKey())) {
            headers.set("Authorization", "Bearer " + properties.getApiKey());
        }
        headers.set("Accept", "application/json");
        return headers;
    }

    protected abstract List<ExternalBankAccount> stubAccounts();

    protected abstract List<ExternalTransaction> stubTransactions(String accountId);
}
