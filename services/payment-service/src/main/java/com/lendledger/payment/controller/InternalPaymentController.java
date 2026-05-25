package com.lendledger.payment.controller;

import com.lendledger.common.Constants;
import com.lendledger.common.error.ApiException;
import com.lendledger.payment.dto.PaymentDtos;
import com.lendledger.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
public class InternalPaymentController {
    private final PaymentService paymentService;
    private final String internalApiKey;

    public InternalPaymentController(PaymentService paymentService,
                                     @Value("${lendledger.internal-api-key}") String internalApiKey) {
        this.paymentService = paymentService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/ledger/disburse")
    public void disburse(
            @RequestHeader(Constants.HEADER_INTERNAL_KEY) String key,
            @Valid @RequestBody PaymentDtos.DisburseRequest req) {
        verify(key);
        paymentService.disburse(req);
    }

    private void verify(String key) {
        if (!internalApiKey.equals(key)) {
            throw new ApiException("FORBIDDEN", "Invalid internal API key", HttpStatus.FORBIDDEN);
        }
    }
}
