//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PolicyValidationTests {
    @Test
    void rejectsInvalidSettingsInsteadOfFailingOnRequests() {
        assertThatThrownBy(() -> policy(0, Duration.ofSeconds(1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy(1, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy(1, Duration.ofDays(2))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RateLimitPolicy(null, 1, Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void namedRegistryRejectsMissingNamesAndSharesOneBudget() {
        RateLimitRegistry registry = new RateLimitRegistry(new RateLimitProperties(Map.of("shared", policy(1, Duration.ofDays(1)))));
        assertThat(registry.acquire("shared").allowed()).isTrue();
        assertThat(registry.acquire("shared").allowed()).isFalse();
        assertThatThrownBy(() -> registry.acquire("missing")).isInstanceOf(IllegalArgumentException.class);
    }

    private RateLimitPolicy policy(int capacity, Duration duration) {
        return new RateLimitPolicy(Algorithm.FIXED_WINDOW, capacity, duration);
    }
}
