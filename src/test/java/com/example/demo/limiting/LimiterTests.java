//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import com.example.demo.support.ConcurrentCalls;
import com.example.demo.support.MutableNanoClock;
import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.assertThat;

class LimiterTests {
    private final MutableNanoClock clock = new MutableNanoClock();

    @ParameterizedTest
    @EnumSource(Algorithm.class)
    void admitsOnlyCapacityAndReportsRetry(Algorithm algorithm) {
        RateLimitStrategy limiter = limiter(algorithm);
        for (int i = 0; i < 3; i++) assertThat(limiter.tryAcquire().allowed()).isTrue();
        LimitDecision denied = limiter.tryAcquire();
        assertThat(denied.allowed()).isFalse();
        assertThat(denied.retrySeconds()).isBetween(1L, 10L);
    }

    @ParameterizedTest
    @EnumSource(Algorithm.class)
    void fullQuotaReturnsAfterPeriod(Algorithm algorithm) {
        RateLimitStrategy limiter = limiter(algorithm);
        for (int i = 0; i < 3; i++) limiter.tryAcquire();
        clock.advance(Duration.ofSeconds(10));
        for (int i = 0; i < 3; i++) assertThat(limiter.tryAcquire().allowed()).isTrue();
        assertThat(limiter.tryAcquire().allowed()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Algorithm.class)
    void concurrentRequestsCannotOverspend(Algorithm algorithm) throws Exception {
        RateLimitStrategy limiter = limiter(algorithm);
        assertThat(ConcurrentCalls.allowed(48, () -> limiter.tryAcquire().allowed())).isEqualTo(3);
    }

    @ParameterizedTest
    @EnumSource(Algorithm.class)
    void separatePoliciesHaveIndependentQuotas(Algorithm algorithm) {
        RateLimitStrategy first = limiter(algorithm);
        for (int i = 0; i < 3; i++) first.tryAcquire();
        assertThat(first.tryAcquire().allowed()).isFalse();
        assertThat(limiter(algorithm).tryAcquire().allowed()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(Algorithm.class)
    void rejectedRequestsDoNotDelayRefill(Algorithm algorithm) {
        RateLimitStrategy limiter = limiter(algorithm);
        for (int i = 0; i < 3; i++) limiter.tryAcquire();
        clock.advance(Duration.ofSeconds(1));
        for (int i = 0; i < 20; i++) assertThat(limiter.tryAcquire().allowed()).isFalse();
        clock.advance(Duration.ofSeconds(9));
        assertThat(limiter.tryAcquire().allowed()).isTrue();
    }

    private RateLimitStrategy limiter(Algorithm algorithm) {
        return LimiterFactory.create(new RateLimitPolicy(algorithm, 3, Duration.ofSeconds(10)), clock);
    }
}
