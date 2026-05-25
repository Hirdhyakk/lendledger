package com.lendledger.loan.controller;

import com.lendledger.common.Constants;
import com.lendledger.common.error.ApiException;
import com.lendledger.loan.dto.LoanDtos;
import com.lendledger.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
public class InternalLoanController {
    private final LoanService loanService;
    private final String internalApiKey;

    public InternalLoanController(LoanService loanService,
                                  @Value("${lendledger.internal-api-key}") String internalApiKey) {
        this.loanService = loanService;
        this.internalApiKey = internalApiKey;
    }

    @GetMapping("/loans/{id}")
    public LoanDtos.InternalLoanResponse getLoan(
            @RequestHeader(Constants.HEADER_INTERNAL_KEY) String key, @PathVariable UUID id) {
        verify(key);
        return loanService.getInternalLoan(id);
    }

    @GetMapping("/loans/{id}/emis/unpaid")
    public List<LoanDtos.EmiResponse> unpaidEmis(
            @RequestHeader(Constants.HEADER_INTERNAL_KEY) String key, @PathVariable UUID id) {
        verify(key);
        return loanService.getUnpaidEmis(id);
    }

    @PostMapping("/loans/{id}/allocations")
    public void applyAllocations(
            @RequestHeader(Constants.HEADER_INTERNAL_KEY) String key,
            @PathVariable UUID id,
            @Valid @RequestBody LoanDtos.ApplyAllocationRequest req) {
        verify(key);
        loanService.applyAllocations(id, req);
    }

    private void verify(String key) {
        if (!internalApiKey.equals(key)) {
            throw new ApiException("FORBIDDEN", "Invalid internal API key", HttpStatus.FORBIDDEN);
        }
    }
}
