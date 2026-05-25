package com.lendledger.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendledger.payment.domain.LedgerEntryType;
import com.lendledger.payment.dto.PaymentDtos;
import com.lendledger.payment.event.RedisEventPublisher;
import com.lendledger.payment.repository.IdempotencyKeyRepository;
import com.lendledger.payment.repository.LedgerEntryRepository;
import com.lendledger.payment.repository.RepaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceDisburseTest {

    @Mock LedgerEntryRepository ledgerRepository;
    @Mock RepaymentRepository repaymentRepository;
    @Mock IdempotencyKeyRepository idempotencyRepository;
    @Mock com.lendledger.payment.client.LoanServiceClient loanClient;
    @Mock RedisEventPublisher eventPublisher;
    @Mock ObjectMapper objectMapper;

    @InjectMocks PaymentService paymentService;

    @Test
    void disburseWritesLedgerEntry() {
        UUID loanId = UUID.randomUUID();
        paymentService.disburse(new PaymentDtos.DisburseRequest(loanId, new BigDecimal("100000"), "REF-1"));
        var captor = ArgumentCaptor.forClass(com.lendledger.payment.domain.LedgerEntryEntity.class);
        verify(ledgerRepository).save(captor.capture());
        assertEquals(loanId, captor.getValue().getLoanId());
        assertEquals(LedgerEntryType.DISBURSE, captor.getValue().getEntryType());
        assertEquals(new BigDecimal("100000"), captor.getValue().getAmount());
    }
}
