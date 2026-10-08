//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import com.example.demo.support.MutableNanoClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AlgorithmBoundaryTests {
    private final MutableNanoClock clock = new MutableNanoClock();

    @Test
    void fixedWindowResetsAtItsBoundary() {
        RateLimitStrategy limiter = create(Algorithm.FIXED_WINDOW);
        clock.advance(Duration.ofSeconds(9));
        assertThat(limiter.tryAcquire().allowed()).isTrue();
        clock.advance(Duration.ofSeconds(1));
        assertThat(limiter.tryAcquire().allowed()).isTrue();
    }

    @Test
    void slidingWindowUsesAcceptanceTimeRatherThanCreationTime() {
        RateLimitStrategy limiter = create(Algorithm.SLIDING_WINDOW);
        clock.advance(Duration.ofSeconds(9));
        limiter.tryAcquire();
        clock.advance(Duration.ofSeconds(1));
        assertThat(limiter.tryAcquire().retrySeconds()).isEqualTo(9);
        clock.advance(Duration.ofSeconds(9));
        assertThat(limiter.tryAcquire().allowed()).isTrue();
    }

    @Test
    void tokenBucketRefillsFractionallyAndCapsAccumulation() {
        RateLimitStrategy limiter = create(Algorithm.TOKEN_BUCKET);
        limiter.tryAcquire();
        clock.advance(Duration.ofSeconds(5));
        assertThat(limiter.tryAcquire().retrySeconds()).isEqualTo(5);
        clock.advance(Duration.ofSeconds(100));
        assertThat(limiter.tryAcquire().allowed()).isTrue();
        assertThat(limiter.tryAcquire().allowed()).isFalse();
    }

    private RateLimitStrategy create(Algorithm algorithm) {
        return LimiterFactory.create(new RateLimitPolicy(algorithm, 1, Duration.ofSeconds(10)), clock);
    }
}
