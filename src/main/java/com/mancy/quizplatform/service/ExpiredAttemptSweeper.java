package com.mancy.quizplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs every minute and closes attempts whose time ran out but whose browser never submitted
 * (tab closed, computer switched off ...). Answers that were never submitted are not stored,
 * so such an attempt is graded as unanswered.
 */
@Component
@ConditionalOnProperty(name = "app.sweeper.enabled", havingValue = "true", matchIfMissing = true)
public class ExpiredAttemptSweeper {

    private static final Logger log = LoggerFactory.getLogger(ExpiredAttemptSweeper.class);

    private final AttemptService attemptService;

    public ExpiredAttemptSweeper(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    public void closeExpiredAttempts() {
        for (Long id : attemptService.findExpiredAttemptIds()) {
            try {
                attemptService.closeExpiredAttempt(id);   // each one in its own transaction
                log.info("Closed expired attempt {}", id);
            } catch (RuntimeException ex) {
                log.warn("Could not close attempt {}", id, ex);
            }
        }
    }
}
