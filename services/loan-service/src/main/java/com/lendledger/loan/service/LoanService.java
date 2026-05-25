package com.lendledger.loan.service;

import com.lendledger.common.error.ApiException;
import com.lendledger.common.security.Role;
import com.lendledger.loan.client.AuthServiceClient;
import com.lendledger.loan.client.PaymentServiceClient;
import com.lendledger.loan.domain.*;
import com.lendledger.loan.dto.LoanDtos;
import com.lendledger.loan.event.RedisEventPublisher;
import com.lendledger.loan.repository.BorrowerRepository;
import com.lendledger.loan.repository.EmiInstallmentRepository;
import com.lendledger.loan.repository.LoanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoanService {
    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;
    private final EmiInstallmentRepository emiRepository;
    private final AuthServiceClient authClient;
    private final PaymentServiceClient paymentClient;
    private final RedisEventPublisher eventPublisher;

    public LoanService(BorrowerRepository borrowerRepository, LoanRepository loanRepository,
                       EmiInstallmentRepository emiRepository, AuthServiceClient authClient,
                       PaymentServiceClient paymentClient, RedisEventPublisher eventPublisher) {
        this.borrowerRepository = borrowerRepository;
        this.loanRepository = loanRepository;
        this.emiRepository = emiRepository;
        this.authClient = authClient;
        this.paymentClient = paymentClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LoanDtos.BorrowerResponse createBorrower(LoanDtos.CreateBorrowerRequest req) {
        Map<String, Object> user = authClient.createUser(req.email(), req.password(), req.fullName(),
                req.phone(), Role.BORROWER);
        UUID userId = UUID.fromString(user.get("id").toString());
        if (borrowerRepository.existsByUserId(userId)) {
            throw new ApiException("BORROWER_EXISTS", "Borrower already exists", HttpStatus.CONFLICT);
        }
        BorrowerEntity b = new BorrowerEntity();
        b.setUserId(userId);
        b.setAddress(req.address());
        b.setPanMasked(req.panMasked());
        borrowerRepository.save(b);
        return toBorrowerResponse(b, user);
    }

    public List<LoanDtos.BorrowerResponse> listBorrowers() {
        return borrowerRepository.findAll().stream().map(b -> {
            Map<String, Object> user = authClient.getUser(b.getUserId());
            return toBorrowerResponse(b, user);
        }).toList();
    }

    @Transactional
    public LoanDtos.LoanResponse createLoan(LoanDtos.CreateLoanRequest req, UUID adminId) {
        borrowerRepository.findById(req.borrowerId())
                .orElseThrow(() -> new ApiException("BORROWER_NOT_FOUND", "Borrower not found", HttpStatus.NOT_FOUND));
        LoanEntity loan = new LoanEntity();
        loan.setBorrowerId(req.borrowerId());
        loan.setPrincipal(req.principal());
        loan.setAnnualRate(req.annualRate());
        loan.setTenureMonths(req.tenureMonths());
        loan.setStatus(LoanStatus.PENDING);
        loan.setOutstandingPrincipal(req.principal());
        loan.setCreatedByAdminId(adminId);
        loanRepository.save(loan);
        return toLoanResponse(loan);
    }

    @Transactional
    public LoanDtos.LoanResponse approveLoan(UUID loanId) {
        LoanEntity loan = getLoanOrThrow(loanId);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ApiException("INVALID_STATE", "Loan must be PENDING to approve", HttpStatus.BAD_REQUEST);
        }
        List<EmiCalculator.EmiRow> schedule = EmiCalculator.generateSchedule(
                loan.getPrincipal(), loan.getAnnualRate(), loan.getTenureMonths(),
                LocalDate.now().plusMonths(1));
        for (EmiCalculator.EmiRow row : schedule) {
            EmiInstallmentEntity emi = new EmiInstallmentEntity();
            emi.setLoanId(loan.getId());
            emi.setInstallmentNo(row.installmentNo());
            emi.setDueDate(row.dueDate());
            emi.setEmiAmount(row.emiAmount());
            emi.setPrincipalComponent(row.principalComponent());
            emi.setInterestComponent(row.interestComponent());
            emi.setStatus(EmiStatus.DUE);
            emiRepository.save(emi);
        }
        loan.setStatus(LoanStatus.APPROVED);
        return toLoanResponse(loan);
    }

    @Transactional
    public LoanDtos.LoanResponse disburseLoan(UUID loanId) {
        LoanEntity loan = getLoanOrThrow(loanId);
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new ApiException("INVALID_STATE", "Loan must be APPROVED to disburse", HttpStatus.BAD_REQUEST);
        }
        paymentClient.disburse(loan.getId(), loan.getPrincipal(), "DISBURSE-" + loan.getId());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedAt(Instant.now());
        loan.setOutstandingPrincipal(loan.getPrincipal());
        BorrowerEntity borrower = borrowerRepository.findById(loan.getBorrowerId()).orElseThrow();
        eventPublisher.loanDisbursed(Map.of(
                "loanId", loan.getId().toString(),
                "userId", borrower.getUserId().toString(),
                "amount", loan.getPrincipal().toString()));
        return toLoanResponse(loan);
    }

    public List<LoanDtos.LoanResponse> listLoans(LoanStatus status) {
        List<LoanEntity> loans = status != null ? loanRepository.findByStatus(status) : loanRepository.findAll();
        return loans.stream().map(this::toLoanResponse).toList();
    }

    public List<LoanDtos.LoanResponse> borrowerLoans(UUID userId) {
        BorrowerEntity borrower = borrowerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("BORROWER_NOT_FOUND", "Borrower profile not found", HttpStatus.NOT_FOUND));
        return loanRepository.findByBorrowerId(borrower.getId()).stream().map(this::toLoanResponse).toList();
    }

    public List<LoanDtos.EmiResponse> getSchedule(UUID loanId, UUID userId, Role role) {
        LoanEntity loan = getLoanOrThrow(loanId);
        assertLoanAccess(loan, userId, role);
        return emiRepository.findByLoanIdOrderByInstallmentNo(loanId).stream().map(this::toEmiResponse).toList();
    }

    public LoanDtos.DashboardStats dashboardStats() {
        long active = loanRepository.findByStatus(LoanStatus.ACTIVE).size();
        long overdue = emiRepository.findOverdueCandidates(LocalDate.now()).size();
        BigDecimal outstanding = loanRepository.findByStatus(LoanStatus.ACTIVE).stream()
                .map(LoanEntity::getOutstandingPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new LoanDtos.DashboardStats(active, overdue, outstanding);
    }

    public List<LoanDtos.OverdueReportItem> overdueReport() {
        List<EmiInstallmentEntity> overdue = emiRepository.findOverdueCandidates(LocalDate.now());
        List<LoanDtos.OverdueReportItem> items = new ArrayList<>();
        for (EmiInstallmentEntity emi : overdue) {
            LoanEntity loan = getLoanOrThrow(emi.getLoanId());
            BorrowerEntity borrower = borrowerRepository.findById(loan.getBorrowerId()).orElseThrow();
            Map<String, Object> user = authClient.getUser(borrower.getUserId());
            items.add(new LoanDtos.OverdueReportItem(
                    loan.getId(), emi.getId(), emi.getInstallmentNo(), emi.getDueDate(),
                    emi.getEmiAmount(), emi.getPaidAmount(), user.get("fullName").toString()));
        }
        return items;
    }

    @Transactional
    public void applyAllocations(UUID loanId, LoanDtos.ApplyAllocationRequest req) {
        LoanEntity loan = getLoanOrThrow(loanId);
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.DEFAULTED) {
            throw new ApiException("INVALID_STATE", "Loan not active", HttpStatus.BAD_REQUEST);
        }
        for (LoanDtos.AllocationItem item : req.allocations()) {
            EmiInstallmentEntity emi = emiRepository.findById(item.emiId())
                    .orElseThrow(() -> new ApiException("EMI_NOT_FOUND", "EMI not found", HttpStatus.NOT_FOUND));
            BigDecimal newPaid = emi.getPaidAmount().add(item.allocatedAmount());
            emi.setPaidAmount(newPaid);
            if (newPaid.compareTo(emi.getEmiAmount()) >= 0) {
                emi.setStatus(EmiStatus.PAID);
                emi.setPaidAmount(emi.getEmiAmount());
            } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
                emi.setStatus(EmiStatus.PARTIAL);
            }
            emiRepository.save(emi);
        }
        loan.setOutstandingPrincipal(loan.getOutstandingPrincipal().subtract(req.totalPrincipalReduced()).max(BigDecimal.ZERO));
        long unpaid = emiRepository.countByLoanIdAndStatusNot(loanId, EmiStatus.PAID);
        if (unpaid == 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setOutstandingPrincipal(BigDecimal.ZERO);
        }
    }

    public LoanDtos.InternalLoanResponse getInternalLoan(UUID loanId) {
        LoanEntity loan = getLoanOrThrow(loanId);
        BorrowerEntity borrower = borrowerRepository.findById(loan.getBorrowerId()).orElseThrow();
        return new LoanDtos.InternalLoanResponse(loan.getId(), loan.getBorrowerId(), borrower.getUserId(),
                loan.getStatus(), loan.getOutstandingPrincipal(), loan.getPrincipal());
    }

    public List<LoanDtos.EmiResponse> getUnpaidEmis(UUID loanId) {
        return emiRepository.findByLoanIdAndStatusIn(loanId,
                List.of(EmiStatus.DUE, EmiStatus.PARTIAL, EmiStatus.OVERDUE))
                .stream().map(this::toEmiResponse).toList();
    }

    @Transactional
    public int markOverdueEmis() {
        List<EmiInstallmentEntity> candidates = emiRepository.findOverdueCandidates(LocalDate.now());
        Set<UUID> loanIds = new HashSet<>();
        for (EmiInstallmentEntity emi : candidates) {
            if (emi.getStatus() != EmiStatus.PAID) {
                emi.setStatus(EmiStatus.OVERDUE);
                emiRepository.save(emi);
                loanIds.add(emi.getLoanId());
            }
        }
        for (UUID loanId : loanIds) {
            LoanEntity loan = getLoanOrThrow(loanId);
            if (loan.getStatus() == LoanStatus.ACTIVE) {
                loan.setStatus(LoanStatus.DEFAULTED);
                BorrowerEntity borrower = borrowerRepository.findById(loan.getBorrowerId()).orElseThrow();
                eventPublisher.loanOverdue(Map.of("loanId", loanId.toString(), "userId", borrower.getUserId().toString()));
            }
        }
        return candidates.size();
    }

    private void assertLoanAccess(LoanEntity loan, UUID userId, Role role) {
        if (role == Role.ADMIN) return;
        BorrowerEntity borrower = borrowerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("FORBIDDEN", "Access denied", HttpStatus.FORBIDDEN));
        if (!loan.getBorrowerId().equals(borrower.getId())) {
            throw new ApiException("FORBIDDEN", "Access denied", HttpStatus.FORBIDDEN);
        }
    }

    private LoanEntity getLoanOrThrow(UUID id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ApiException("LOAN_NOT_FOUND", "Loan not found", HttpStatus.NOT_FOUND));
    }

    private LoanDtos.BorrowerResponse toBorrowerResponse(BorrowerEntity b, Map<String, Object> user) {
        return new LoanDtos.BorrowerResponse(b.getId(), b.getUserId(),
                user.get("fullName").toString(), user.get("email").toString(),
                b.getAddress(), b.getPanMasked(), b.getStatus());
    }

    private LoanDtos.LoanResponse toLoanResponse(LoanEntity loan) {
        return new LoanDtos.LoanResponse(loan.getId(), loan.getBorrowerId(), loan.getPrincipal(),
                loan.getAnnualRate(), loan.getTenureMonths(), loan.getStatus(),
                loan.getOutstandingPrincipal(), loan.getDisbursedAt(), loan.getCreatedAt());
    }

    private LoanDtos.EmiResponse toEmiResponse(EmiInstallmentEntity emi) {
        return new LoanDtos.EmiResponse(emi.getId(), emi.getInstallmentNo(), emi.getDueDate(),
                emi.getEmiAmount(), emi.getPrincipalComponent(), emi.getInterestComponent(),
                emi.getPaidAmount(), emi.getStatus());
    }
}
