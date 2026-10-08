//Copyright ANuj Nautiyal
package com.example.demo.controller;

import com.example.demo.annotations.RateLimiter;
import com.example.demo.weather.WeatherReport;
import com.example.demo.weather.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** A token-bucket quota protects requests before the weather service runs. */
@RestController
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weather;

    /**
     * @param city provider location
     * @return current weather
     */
    @GetMapping("/weather")
    @RateLimiter("weather")
    public WeatherReport current(@RequestParam String city) {
        return weather.current(city);
    }
}
