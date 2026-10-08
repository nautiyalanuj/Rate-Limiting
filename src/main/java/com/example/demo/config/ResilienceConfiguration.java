//Copyright ANuj Nautiyal
package com.example.demo.config;

import com.example.demo.limiting.RateLimitProperties;
import com.example.demo.resilience.BreakerProperties;
import com.example.demo.weather.WeatherProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Bind immutable, validated settings once at startup. */
@Configuration
@EnableConfigurationProperties({RateLimitProperties.class, BreakerProperties.class, WeatherProperties.class})
public class ResilienceConfiguration { }
