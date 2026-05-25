package com.lendledger.loan.client;

import com.lendledger.common.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentServiceClient {
    private final RestClient restClient;
    private final String internalApiKey;

    public PaymentServiceClient(@Value("${lendledger.payment-service-url}") String baseUrl,
                                @Value("${lendledger.internal-api-key}") String internalApiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public void disburse(UUID loanId, BigDecimal amount, String reference) {
        restClient.post()
                .uri("/internal/ledger/disburse")
                .header(Constants.HEADER_INTERNAL_KEY, internalApiKey)
                .body(Map.of("loanId", loanId.toString(), "amount", amount, "reference", reference))
                .retrieve()
                .toBodilessEntity();
    }
}
