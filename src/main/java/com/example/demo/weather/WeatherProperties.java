//Copyright ANuj Nautiyal
package com.example.demo.weather;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** The API key comes from an environment variable, never source control. */
@ConfigurationProperties("app.weather")
public record WeatherProperties(URI baseUrl, String apiKey, Duration connectTimeout, Duration readTimeout) {
    public WeatherProperties {
        if (baseUrl == null || !baseUrl.isAbsolute() || !java.util.Set.of("http", "https").contains(baseUrl.getScheme())) {
            throw new IllegalArgumentException("Weather base URL must be an absolute HTTP(S) URL");
        }
        if (connectTimeout == null || readTimeout == null || connectTimeout.isNegative() || readTimeout.isNegative()
                || connectTimeout.isZero() || readTimeout.isZero()) {
            throw new IllegalArgumentException("Weather timeouts must be positive");
        }
    }
}
