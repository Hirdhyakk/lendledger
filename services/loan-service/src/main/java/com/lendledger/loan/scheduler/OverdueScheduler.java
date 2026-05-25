package com.lendledger.loan.scheduler;

import com.lendledger.loan.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueScheduler {
    private static final Logger log = LoggerFactory.getLogger(OverdueScheduler.class);
    private final LoanService loanService;

    public OverdueScheduler(LoanService loanService) {
        this.loanService = loanService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void markOverdue() {
        int count = loanService.markOverdueEmis();
        log.info("Marked {} overdue EMIs", count);
    }
}
