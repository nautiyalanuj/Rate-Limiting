//Copyright ANuj Nautiyal
package com.example.demo.weather;

import com.example.demo.resilience.BreakerProperties;
import com.example.demo.resilience.Breakers;
import com.example.demo.resilience.DependencyUnavailable;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherServiceTests {
    private final WeatherClient client = mock(WeatherClient.class);
    private final Breakers breakers = new Breakers(new BreakerProperties(2, 2, 50, Duration.ofSeconds(30)));

    @Test
    void failuresOpenWeatherCircuitAndFurtherCallsNeverReachClient() {
        WeatherService service = service("test-key");
        when(client.current("London")).thenThrow(new DependencyUnavailable());
        for (int i = 0; i < 2; i++) assertThatThrownBy(() -> service.current("London"))
                .isInstanceOf(DependencyUnavailable.class);
        assertThatThrownBy(() -> service.current("London")).isInstanceOf(CallNotPermittedException.class);
        verify(client, times(2)).current("London");
    }

    @Test
    void blankCityAndMissingKeyDoNotCallProviderOrOpenCircuit() {
        assertThatThrownBy(() -> service("test-key").current("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service("").current("London")).isInstanceOf(WeatherNotConfigured.class);
        assertThat(breakers.state("weather").name()).isEqualTo("CLOSED");
        verifyNoInteractions(client);
    }

    @Test
    void invalidProviderLocationDoesNotCountAsAServiceFailure() {
        when(client.current("unknown")).thenThrow(new IllegalArgumentException());
        for (int i = 0; i < 5; i++) assertThatThrownBy(() -> service("test-key").current("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(breakers.state("weather").name()).isEqualTo("CLOSED");
    }

    @Test
    void trimsCityAndReturnsActualProviderReport() {
        WeatherReport report = new WeatherReport("London", "UK", 18, "Sunny");
        when(client.current("London")).thenReturn(report);
        assertThat(service("test-key").current(" London ")).isEqualTo(report);
    }

    private WeatherService service(String key) {
        WeatherProperties settings = new WeatherProperties(URI.create("https://api.weatherapi.com/v1"), key,
                Duration.ofSeconds(1), Duration.ofSeconds(1));
        return new WeatherService(client, settings, breakers);
    }
}
