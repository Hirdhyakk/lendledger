package com.lendledger.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendledger.common.error.ApiException;
import com.lendledger.common.security.Role;
import com.lendledger.payment.client.LoanServiceClient;
import com.lendledger.payment.domain.*;
import com.lendledger.payment.dto.PaymentDtos;
import com.lendledger.payment.event.RedisEventPublisher;
import com.lendledger.payment.repository.IdempotencyKeyRepository;
import com.lendledger.payment.repository.LedgerEntryRepository;
import com.lendledger.payment.repository.RepaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class PaymentService {
    private final LedgerEntryRepository ledgerRepository;
    private final RepaymentRepository repaymentRepository;
    private final IdempotencyKeyRepository idempotencyRepository;
    private final LoanServiceClient loanClient;
    private final RedisEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public PaymentService(LedgerEntryRepository ledgerRepository, RepaymentRepository repaymentRepository,
                          IdempotencyKeyRepository idempotencyRepository, LoanServiceClient loanClient,
                          RedisEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.ledgerRepository = ledgerRepository;
        this.repaymentRepository = repaymentRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.loanClient = loanClient;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void disburse(PaymentDtos.DisburseRequest req) {
        LedgerEntryEntity entry = new LedgerEntryEntity();
        entry.setLoanId(req.loanId());
        entry.setEntryType(LedgerEntryType.DISBURSE);
        entry.setAmount(req.amount());
        entry.setReference(req.reference());
        ledgerRepository.save(entry);
    }

    @Transactional
    public PaymentDtos.RepaymentResponse repay(UUID loanId, UUID userId, Role role,
                                               String idempotencyKey, PaymentDtos.RepayRequest req) {
        Optional<IdempotencyKeyEntity> cached = idempotencyRepository.findById(idempotencyKey);
        if (cached.isPresent()) {
            return deserialize(cached.get().getResponseBody());
        }
        Map<String, Object> loan = loanClient.getLoan(loanId);
        assertBorrowerAccess(loan, userId, role);
        String status = loan.get("status").toString();
        if (!"ACTIVE".equals(status) && !"DEFAULTED".equals(status)) {
            throw new ApiException("INVALID_STATE", "Loan not repayable", HttpStatus.BAD_REQUEST);
        }

        List<Map<String, Object>> emis = loanClient.getUnpaidEmis(loanId);
        BigDecimal remaining = req.amount();
        List<PaymentDtos.AllocationResult> results = new ArrayList<>();
        List<PaymentDtos.AllocationItem> allocationItems = new ArrayList<>();
        BigDecimal principalReduced = BigDecimal.ZERO;

        for (Map<String, Object> emi : emis) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal emiAmount = new BigDecimal(emi.get("emiAmount").toString());
            BigDecimal paid = new BigDecimal(emi.get("paidAmount").toString());
            BigDecimal due = emiAmount.subtract(paid);
            BigDecimal alloc = remaining.min(due);
            if (alloc.compareTo(BigDecimal.ZERO) <= 0) continue;
            UUID emiId = UUID.fromString(emi.get("id").toString());
            allocationItems.add(new PaymentDtos.AllocationItem(emiId, alloc));
            principalReduced = principalReduced.add(new BigDecimal(emi.get("principalComponent").toString())
                    .multiply(alloc.divide(emiAmount, 8, java.math.RoundingMode.HALF_UP)));
            results.add(new PaymentDtos.AllocationResult(emiId, alloc));
            remaining = remaining.subtract(alloc);
        }

        RepaymentEntity repayment = new RepaymentEntity();
        repayment.setLoanId(loanId);
        repayment.setAmount(req.amount().subtract(remaining));
        repayment.setIdempotencyKey(idempotencyKey);
        repayment.setPaymentRef(req.paymentRef());
        repayment.setStatus("COMPLETED");
        repaymentRepository.save(repayment);

        LedgerEntryEntity ledger = new LedgerEntryEntity();
        ledger.setLoanId(loanId);
        ledger.setEntryType(LedgerEntryType.REPAYMENT);
        ledger.setAmount(repayment.getAmount().negate());
        ledger.setReference(req.paymentRef());
        ledgerRepository.save(ledger);

        if (!allocationItems.isEmpty()) {
            loanClient.applyAllocations(loanId,
                    new PaymentDtos.ApplyAllocationRequest(allocationItems, principalReduced));
        }

        PaymentDtos.RepaymentResponse response = new PaymentDtos.RepaymentResponse(
                repayment.getId(), loanId, repayment.getAmount(), repayment.getStatus(), results);

        storeIdempotency(idempotencyKey, response);
        eventPublisher.paymentReceived(Map.of(
                "loanId", loanId.toString(),
                "userId", loan.get("borrowerUserId").toString(),
                "amount", repayment.getAmount().toString()));
        return response;
    }

    public PaymentDtos.StatementResponse statement(UUID loanId, UUID userId, Role role) {
        Map<String, Object> loan = loanClient.getLoan(loanId);
        assertBorrowerAccess(loan, userId, role);
        List<PaymentDtos.LedgerEntryResponse> ledger = ledgerRepository.findByLoanIdOrderByCreatedAtAsc(loanId)
                .stream().map(this::toLedger).toList();
        List<PaymentDtos.RepaymentSummary> repayments = repaymentRepository.findByLoanIdOrderByCreatedAtDesc(loanId)
                .stream()
                .map(r -> new PaymentDtos.RepaymentSummary(r.getId(), r.getAmount(), r.getPaymentRef(), r.getCreatedAt()))
                .toList();
        return new PaymentDtos.StatementResponse(loanId, ledger, repayments);
    }

    public PaymentDtos.CollectionsReport collections(LocalDate from, LocalDate to) {
        Instant start = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        BigDecimal total = repaymentRepository.sumAmountBetween(start, end);
        return new PaymentDtos.CollectionsReport(total, start, end);
    }

    @SuppressWarnings("unchecked")
    private void assertBorrowerAccess(Map<String, Object> loan, UUID userId, Role role) {
        if (role == Role.ADMIN) return;
        UUID borrowerUserId = UUID.fromString(loan.get("borrowerUserId").toString());
        if (!borrowerUserId.equals(userId)) {
            throw new ApiException("FORBIDDEN", "Access denied", HttpStatus.FORBIDDEN);
        }
    }

    private void storeIdempotency(String key, PaymentDtos.RepaymentResponse response) {
        try {
            IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
            entity.setKey(key);
            entity.setResponseBody(objectMapper.writeValueAsString(response));
            entity.setExpiresAt(Instant.now().plusSeconds(86400));
            idempotencyRepository.save(entity);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private PaymentDtos.RepaymentResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, PaymentDtos.RepaymentResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private PaymentDtos.LedgerEntryResponse toLedger(LedgerEntryEntity e) {
        return new PaymentDtos.LedgerEntryResponse(e.getId(), e.getLoanId(), e.getEntryType(),
                e.getAmount(), e.getReference(), e.getCreatedAt());
    }
}
