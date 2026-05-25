package com.lendledger.loan.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmiCalculatorTest {

    @Test
    void generatesCorrectInstallmentCount() {
        List<EmiCalculator.EmiRow> rows = EmiCalculator.generateSchedule(
                new BigDecimal("100000"), new BigDecimal("12"), 12, LocalDate.of(2026, 6, 1));
        assertEquals(12, rows.size());
        assertEquals(1, rows.get(0).installmentNo());
        BigDecimal totalPrincipal = rows.stream()
                .map(EmiCalculator.EmiRow::principalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertTrue(totalPrincipal.subtract(new BigDecimal("100000")).abs().compareTo(new BigDecimal("1")) <= 0);
    }

    @Test
    void zeroInterestSplitsPrincipalEvenly() {
        List<EmiCalculator.EmiRow> rows = EmiCalculator.generateSchedule(
                new BigDecimal("12000"), BigDecimal.ZERO, 12, LocalDate.of(2026, 1, 1));
        assertEquals(12, rows.size());
        assertEquals(0, new BigDecimal("1000.00").compareTo(rows.get(0).emiAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(rows.get(0).interestComponent()));
    }

    @Test
    void dueDatesIncrementMonthly() {
        List<EmiCalculator.EmiRow> rows = EmiCalculator.generateSchedule(
                new BigDecimal("50000"), new BigDecimal("10"), 3, LocalDate.of(2026, 3, 15));
        assertEquals(LocalDate.of(2026, 3, 15), rows.get(0).dueDate());
        assertEquals(LocalDate.of(2026, 4, 15), rows.get(1).dueDate());
        assertEquals(LocalDate.of(2026, 5, 15), rows.get(2).dueDate());
    }
}
