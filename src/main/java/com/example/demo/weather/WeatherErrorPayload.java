//Copyright ANuj Nautiyal
package com.example.demo.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Provider codes distinguish invalid locations from actual service failures. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherErrorPayload(Error error) {
    /** The message is intentionally discarded so secrets never reach callers. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Error(int code) { }
}
