package com.lendledger.loan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EmiCalculator {

    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);

    private EmiCalculator() {}

    public record EmiRow(int installmentNo, LocalDate dueDate, BigDecimal emiAmount,
                         BigDecimal principalComponent, BigDecimal interestComponent) {}

    public static List<EmiRow> generateSchedule(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths,
                                                LocalDate firstDueDate) {
        if (tenureMonths <= 0) throw new IllegalArgumentException("tenure must be positive");
        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(100), MC)
                .divide(BigDecimal.valueOf(12), MC);

        BigDecimal emi;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            emi = principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusI = BigDecimal.ONE.add(monthlyRate);
            BigDecimal pow = onePlusI.pow(tenureMonths, MC);
            emi = principal.multiply(monthlyRate).multiply(pow, MC)
                    .divide(pow.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        }

        List<EmiRow> rows = new ArrayList<>();
        BigDecimal outstanding = principal;
        LocalDate due = firstDueDate;

        for (int n = 1; n <= tenureMonths; n++) {
            BigDecimal interest = outstanding.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = emi.subtract(interest).setScale(2, RoundingMode.HALF_UP);
            if (n == tenureMonths) {
                principalPart = outstanding;
                emi = principalPart.add(interest);
            }
            rows.add(new EmiRow(n, due, emi, principalPart, interest));
            outstanding = outstanding.subtract(principalPart).setScale(2, RoundingMode.HALF_UP);
            due = due.plusMonths(1);
        }
        return rows;
    }
}
