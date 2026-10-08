//Copyright ANuj Nautiyal
package com.example.demo.weather;

import com.example.demo.resilience.DependencyUnavailable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Built once and shared safely: request-specific data lives in local variables. */
@Component
@RequiredArgsConstructor
public class WeatherClient {
    private final RestClient weatherRestClient;
    private final WeatherProperties settings;

    /**
     * @param city already validated location, URI-encoded as a query value
     * @return current weather reported by the provider
     * @throws IllegalArgumentException if the provider reports location code 1006
     * @throws DependencyUnavailable on transport, authorization, quota or payload failures
     */
    public WeatherReport current(String city) {
        try {
            return read(city);
        } catch (RestClientResponseException error) {
            if (invalidLocation(error)) throw new IllegalArgumentException("Unknown weather location");
            throw new DependencyUnavailable();
        } catch (RestClientException error) {
            throw new DependencyUnavailable();
        }
    }

    // Only provider code 1006 means a caller supplied an unknown location.
    private boolean invalidLocation(RestClientResponseException error) {
        if (error.getStatusCode().value() != 400) return false;
        try {
            WeatherErrorPayload payload = error.getResponseBodyAs(WeatherErrorPayload.class);
            return payload != null && payload.error() != null && payload.error().code() == 1006;
        } catch (RuntimeException malformedBody) { return false; }
    }

    private WeatherReport read(String city) {
        WeatherPayload payload = weatherRestClient.get().uri(builder -> builder.path("/current.json")
                .queryParam("key", "{key}").queryParam("q", "{city}").queryParam("aqi", "no")
                .build(settings.apiKey(), city)).retrieve().body(WeatherPayload.class);
        if (payload == null) throw new DependencyUnavailable();
        return payload.toReport();
    }
}
