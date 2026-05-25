package com.lendledger.loan.controller;

import com.lendledger.common.Constants;
import com.lendledger.common.security.Role;
import com.lendledger.loan.domain.LoanStatus;
import com.lendledger.loan.dto.LoanDtos;
import com.lendledger.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminLoanController {
    private final LoanService loanService;

    public AdminLoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrowers")
    public LoanDtos.BorrowerResponse createBorrower(@Valid @RequestBody LoanDtos.CreateBorrowerRequest req) {
        return loanService.createBorrower(req);
    }

    @GetMapping("/borrowers")
    public List<LoanDtos.BorrowerResponse> listBorrowers() {
        return loanService.listBorrowers();
    }

    @PostMapping("/loans")
    public LoanDtos.LoanResponse createLoan(
            @RequestHeader(Constants.HEADER_USER_ID) UUID adminId,
            @Valid @RequestBody LoanDtos.CreateLoanRequest req) {
        return loanService.createLoan(req, adminId);
    }

    @PostMapping("/loans/{id}/approve")
    public LoanDtos.LoanResponse approve(@PathVariable UUID id) {
        return loanService.approveLoan(id);
    }

    @PostMapping("/loans/{id}/disburse")
    public LoanDtos.LoanResponse disburse(@PathVariable UUID id) {
        return loanService.disburseLoan(id);
    }

    @GetMapping("/loans")
    public List<LoanDtos.LoanResponse> listLoans(@RequestParam(required = false) LoanStatus status) {
        return loanService.listLoans(status);
    }

    @GetMapping("/loans/{id}/schedule")
    public List<LoanDtos.EmiResponse> schedule(
            @PathVariable UUID id,
            @RequestHeader(Constants.HEADER_USER_ID) UUID userId,
            @RequestHeader(Constants.HEADER_ROLE) Role role) {
        return loanService.getSchedule(id, userId, role);
    }

    @GetMapping("/dashboard/stats")
    public LoanDtos.DashboardStats stats() {
        return loanService.dashboardStats();
    }

    @GetMapping("/reports/overdue")
    public List<LoanDtos.OverdueReportItem> overdue() {
        return loanService.overdueReport();
    }
}
