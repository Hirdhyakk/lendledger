package com.lendledger.payment.client;

import com.lendledger.common.Constants;
import com.lendledger.payment.dto.PaymentDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class LoanServiceClient {
    private final RestClient restClient;
    private final String internalApiKey;

    public LoanServiceClient(@Value("${lendledger.loan-service-url}") String baseUrl,
                             @Value("${lendledger.internal-api-key}") String internalApiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public Map<String, Object> getLoan(UUID loanId) {
        return restClient.get()
                .uri("/internal/loans/{id}", loanId)
                .header(Constants.HEADER_INTERNAL_KEY, internalApiKey)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<Map<String, Object>> getUnpaidEmis(UUID loanId) {
        return restClient.get()
                .uri("/internal/loans/{id}/emis/unpaid", loanId)
                .header(Constants.HEADER_INTERNAL_KEY, internalApiKey)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void applyAllocations(UUID loanId, PaymentDtos.ApplyAllocationRequest req) {
        restClient.post()
                .uri("/internal/loans/{id}/allocations", loanId)
                .header(Constants.HEADER_INTERNAL_KEY, internalApiKey)
                .body(req)
                .retrieve()
                .toBodilessEntity();
    }
}
