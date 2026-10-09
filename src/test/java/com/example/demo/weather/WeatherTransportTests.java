//Copyright ANuj Nautiyal
package com.example.demo.weather;

import com.example.demo.config.WeatherHttpConfiguration;
import com.example.demo.resilience.DependencyUnavailable;
import com.example.demo.support.WeatherStub;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class WeatherTransportTests {
    @Test
    void actualHttpClientCallsLocalProvider() throws Exception {
        try (WeatherStub stub = new WeatherStub(false)) {
            assertThat(client(stub, Duration.ofSeconds(2)).current("London").temperatureCelsius()).isEqualTo(18);
            assertThat(stub.received()).isTrue();
        }
    }

    @Test
    void actualHttpClientTimesOutWhileProviderIsStalled() throws Exception {
        try (WeatherStub stub = new WeatherStub(false)) {
            WeatherClient client = client(stub, Duration.ofSeconds(2));
            client.current("London");
            stub.stall();
            assertThatThrownBy(() -> client.current("London"))
                    .isInstanceOf(DependencyUnavailable.class);
            assertThat(stub.requests()).isEqualTo(2);
        }
    }

    private WeatherClient client(WeatherStub stub, Duration timeout) {
        WeatherProperties settings = new WeatherProperties(stub.baseUrl(), "local-test-key", Duration.ofSeconds(2), timeout);
        return new WeatherClient(new WeatherHttpConfiguration().weatherRestClient(settings,
                org.springframework.web.client.RestClient.builder()), settings);
    }
}
