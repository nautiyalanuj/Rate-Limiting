//Copyright ANuj Nautiyal
package com.example.demo.weather;

import com.example.demo.resilience.Breakers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Validate locally, then protect the provider call with its own circuit. */
@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WeatherClient client;
    private final WeatherProperties settings;
    private final Breakers breakers;

    /**
     * @param city a location containing 1 to 100 characters
     * @return live weather, with no invented fallback values
     * @throws IllegalArgumentException for invalid input
     * @throws WeatherNotConfigured when no API key is configured
     */
    public WeatherReport current(String city) {
        String location = validated(city);
        if (settings.apiKey() == null || settings.apiKey().isBlank()) throw new WeatherNotConfigured();
        // Validation/configuration errors are outside the breaker: no provider call failed.
        return breakers.execute("weather", () -> client.current(location));
    }

    private String validated(String city) {
        if (city == null || city.isBlank() || city.length() > 100) {
            throw new IllegalArgumentException("City must contain 1 to 100 characters");
        }
        return city.strip();
    }
}
