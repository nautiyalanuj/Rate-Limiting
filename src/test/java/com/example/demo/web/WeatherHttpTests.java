//Copyright ANuj Nautiyal
package com.example.demo.web;

import com.example.demo.weather.WeatherClient;
import com.example.demo.weather.WeatherReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.weather.api-key=test-key", "app.rate-limits.policies.weather.capacity=6",
        "app.rate-limits.policies.weather.period=1d", "app.circuit-breaker.minimum-calls=2"})
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class WeatherHttpTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private WeatherClient client;

    @Test
    void weatherIsRateLimitedBeforeItCanReachTheProvider() throws Exception {
        when(client.current("London")).thenReturn(new WeatherReport("London", "UK", 18, "Sunny"));
        for (int i = 0; i < 6; i++) mvc.perform(get("/weather").param("city", "London")).andExpect(status().isOk());
        mvc.perform(get("/weather").param("city", "London")).andExpect(status().isTooManyRequests());
        verify(client, times(6)).current("London");
    }
}
