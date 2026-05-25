package com.lendledger.payment.controller;

import com.lendledger.common.Constants;
import com.lendledger.common.security.Role;
import com.lendledger.payment.dto.PaymentDtos;
import com.lendledger.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/borrower/loans/{id}/repay")
    public PaymentDtos.RepaymentResponse repay(
            @PathVariable UUID id,
            @RequestHeader(Constants.HEADER_USER_ID) UUID userId,
            @RequestHeader(Constants.HEADER_ROLE) Role role,
            @RequestHeader(Constants.HEADER_IDEMPOTENCY) String idempotencyKey,
            @Valid @RequestBody PaymentDtos.RepayRequest req) {
        return paymentService.repay(id, userId, role, idempotencyKey, req);
    }

    @GetMapping("/borrower/loans/{id}/statement")
    public PaymentDtos.StatementResponse statement(
            @PathVariable UUID id,
            @RequestHeader(Constants.HEADER_USER_ID) UUID userId,
            @RequestHeader(Constants.HEADER_ROLE) Role role) {
        return paymentService.statement(id, userId, role);
    }

    @GetMapping("/admin/reports/collections")
    public PaymentDtos.CollectionsReport collections(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return paymentService.collections(from, to);
    }
}
