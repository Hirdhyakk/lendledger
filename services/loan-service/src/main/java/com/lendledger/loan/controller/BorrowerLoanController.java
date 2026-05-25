package com.lendledger.loan.controller;

import com.lendledger.common.Constants;
import com.lendledger.common.security.Role;
import com.lendledger.loan.dto.LoanDtos;
import com.lendledger.loan.service.LoanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/borrower")
public class BorrowerLoanController {
    private final LoanService loanService;

    public BorrowerLoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/loans")
    public List<LoanDtos.LoanResponse> myLoans(@RequestHeader(Constants.HEADER_USER_ID) UUID userId) {
        return loanService.borrowerLoans(userId);
    }

    @GetMapping("/loans/{id}/schedule")
    public List<LoanDtos.EmiResponse> schedule(
            @PathVariable UUID id,
            @RequestHeader(Constants.HEADER_USER_ID) UUID userId,
            @RequestHeader(Constants.HEADER_ROLE) Role role) {
        return loanService.getSchedule(id, userId, role);
    }
}
