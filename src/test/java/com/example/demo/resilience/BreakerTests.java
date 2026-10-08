//Copyright ANuj Nautiyal
package com.example.demo.resilience;

import com.example.demo.support.ConcurrentCalls;
import com.example.demo.support.HeldProbe;
import com.example.demo.support.MutableClock;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class BreakerTests {
    private final MutableClock clock = new MutableClock();
    private final Breakers breakers = new Breakers(new BreakerProperties(2, 2, 50, Duration.ofSeconds(5)), clock);

    @Test
    void opensAtFailureThresholdAndRefusesWithoutRunningAction() {
        breakers.execute("weather", () -> "ok");
        fail();
        AtomicInteger calls = new AtomicInteger();
        assertThatThrownBy(() -> breakers.execute("weather", calls::incrementAndGet))
                .isInstanceOf(CallNotPermittedException.class);
        assertThat(calls.get()).isZero();
        assertThat(breakers.state("weather").name()).isEqualTo("OPEN");
    }

    @Test
    void cooldownThenSuccessfulProbeClosesCircuit() {
        open();
        clock.advance(Duration.ofSeconds(4));
        assertThatThrownBy(() -> breakers.execute("weather", () -> "early"))
                .isInstanceOf(CallNotPermittedException.class);
        clock.advance(Duration.ofSeconds(2));
        assertThat(breakers.execute("weather", () -> "recovered")).isEqualTo("recovered");
        assertThat(breakers.state("weather").name()).isEqualTo("CLOSED");
    }

    @Test
    void failedProbeRestartsCooldown() {
        open();
        clock.advance(Duration.ofSeconds(6));
        fail();
        assertThat(breakers.state("weather").name()).isEqualTo("OPEN");
        assertThatThrownBy(() -> breakers.execute("weather", () -> "early"))
                .isInstanceOf(CallNotPermittedException.class);
    }

    @Test
    void onlyOneConcurrentRecoveryProbeIsAdmitted() throws Exception {
        open();
        clock.advance(Duration.ofSeconds(6));
        try (HeldProbe probe = new HeldProbe(breakers)) {
            assertThat(probe.started()).isTrue();
            assertThat(breakers.state("weather").name()).isEqualTo("HALF_OPEN");
            assertThat(ConcurrentCalls.allowed(32, this::attempt)).isZero();
            assertThat(probe.finish()).isEqualTo("recovered");
        }
    }

    @Test
    void weatherFailureDoesNotOpenOtherDependencies() {
        open();
        assertThat(breakers.execute("database", () -> "ok")).isEqualTo("ok");
        assertThat(breakers.state("database").name()).isEqualTo("CLOSED");
        assertThat(breakers.state("demo").name()).isEqualTo("CLOSED");
    }

    private boolean attempt() {
        try { breakers.execute("weather", () -> "unexpected"); return true; }
        catch (CallNotPermittedException error) { return false; }
    }

    private void open() {
        fail();
        fail();
    }

    private void fail() {
        assertThatThrownBy(() -> breakers.execute("weather", () -> { throw new DependencyUnavailable(); }))
                .isInstanceOf(DependencyUnavailable.class);
    }
}
