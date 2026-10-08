//Copyright ANuj Nautiyal
package com.example.demo.weather;

/** A missing local key is a configuration error, not a provider outage. */
public class WeatherNotConfigured extends RuntimeException {
    /** Create an error containing only the environment variable the caller should set. */
    public WeatherNotConfigured() {
        super("Set WEATHER_API_KEY to enable weather requests");
    }
}
