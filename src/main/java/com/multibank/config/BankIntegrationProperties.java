package com.multibank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "multibank")
public class BankIntegrationProperties {

    private final BankProperties bcr = new BankProperties();
    private final BankProperties bt = new BankProperties();

    public BankProperties getBcr() {
        return bcr;
    }

    public BankProperties getBt() {
        return bt;
    }

    public static class BankProperties {
        private String baseUrl;
        private String apiKey;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
