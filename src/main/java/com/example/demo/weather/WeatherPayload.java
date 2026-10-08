//Copyright ANuj Nautiyal
package com.example.demo.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Match only the fields we need; the provider can add unrelated fields. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherPayload(Location location, Current current) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String name, String country) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(@JsonProperty("temp_c") Double temperature, Condition condition) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Condition(String text) { }

    /**
     * @return mapped weather
     * @throws com.example.demo.resilience.DependencyUnavailable for incomplete data
     */
    public WeatherReport toReport() {
        if (location == null || current == null || current.condition() == null || current.temperature() == null
                || location.name() == null || location.country() == null || current.condition().text() == null) {
            throw new com.example.demo.resilience.DependencyUnavailable();
        }
        return new WeatherReport(location.name(), location.country(), current.temperature(), current.condition().text());
    }
}
