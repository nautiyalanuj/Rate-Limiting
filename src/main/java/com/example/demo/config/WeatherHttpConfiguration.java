//Copyright ANuj Nautiyal
package com.example.demo.config;

import com.example.demo.weather.WeatherProperties;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Reuse one HTTP client and enforce finite network timeouts. */
@Configuration
public class WeatherHttpConfiguration {
    /**
     * @param settings provider URL and timeouts
     * @return a reusable thread-safe client
     */
    @Bean
    public RestClient weatherRestClient(WeatherProperties settings) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(settings.connectTimeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(settings.readTimeout());
        return RestClient.builder().baseUrl(settings.baseUrl().toString()).requestFactory(factory).build();
    }
}
