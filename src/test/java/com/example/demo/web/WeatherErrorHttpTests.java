//Copyright ANuj Nautiyal
package com.example.demo.web;

import com.example.demo.weather.WeatherClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.weather.api-key=")
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class WeatherErrorHttpTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private WeatherClient client;

    @Test
    void missingKeyReturnsUseful503() throws Exception {
        mvc.perform(get("/weather").param("city", "London")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("WEATHER_NOT_CONFIGURED"));
        verifyNoInteractions(client);
    }

    @Test
    void invalidCityReturns400BeforeCallingProvider() throws Exception {
        mvc.perform(get("/weather").param("city", " ")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        verifyNoInteractions(client);
    }
}
