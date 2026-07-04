package com.tool.aidubbing.scheduler;

import com.tool.aidubbing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuotaResetScheduler {

    private final UserRepository userRepository;

    // chạy mỗi ngày lúc 00:05 sáng
    @Scheduled(cron = "0 5 0 * * *")
    public void resetExpiredQuota() {
        Instant now = Instant.now();

        userRepository.findAll().forEach(user -> {
            if (user.getQuotaResetAt() != null && now.isAfter(user.getQuotaResetAt())) {
                log.info("Resetting expired quota for userId={}", user.getId());
                user.setQuotaMinutesLeft(0);
                userRepository.save(user);
            }
        });
    }
}