//Copyright ANuj Nautiyal
package com.example.demo.weather;

import com.example.demo.resilience.DependencyUnavailable;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WeatherClientTests {
    private final RestClient.Builder builder = RestClient.builder().baseUrl("https://api.weatherapi.com/v1");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final WeatherClient client = new WeatherClient(builder.build(), settings());

    @Test
    void sendsProviderKeyAndEncodedCityAndMapsResponse() {
        server.expect(requestTo("https://api.weatherapi.com/v1/current.json?key=test-key&q=New%20Delhi&aqi=no"))
                .andExpect(queryParam("key", "test-key")).andRespond(withSuccess(payload(), MediaType.APPLICATION_JSON));
        assertThat(client.current("New Delhi")).isEqualTo(new WeatherReport("New Delhi", "India", 28.5, "Sunny"));
        server.verify();
    }

    @Test
    void upstreamFailureIsSanitized() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("SECRET test-key"));
        assertThatThrownBy(() -> client.current("London")).isInstanceOf(DependencyUnavailable.class)
                .hasMessageNotContaining("test-key");
        server.verify();
    }

    @Test
    void provider400IsInvalidInputRatherThanAnOutage() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":{\"code\":1006}}"));
        assertThatThrownBy(() -> client.current("unknown")).isInstanceOf(IllegalArgumentException.class);
        server.verify();
    }

    @Test
    void malformedOrIncompleteWeatherIsRejected() {
        server.expect(anything()).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.current("London")).isInstanceOf(DependencyUnavailable.class);
        server.verify();
    }

    @Test
    void emptyBodyIsRejected() {
        server.expect(anything()).andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.current("London")).isInstanceOf(DependencyUnavailable.class);
        server.verify();
    }

    @Test
    void networkTimeoutIsSanitized() {
        server.expect(anything()).andRespond(withException(new java.net.SocketTimeoutException("test-key")));
        assertThatThrownBy(() -> client.current("London")).isInstanceOf(DependencyUnavailable.class)
                .hasMessageNotContaining("test-key");
        server.verify();
    }

    private WeatherProperties settings() {
        return new WeatherProperties(URI.create("https://api.weatherapi.com/v1"), "test-key",
                Duration.ofSeconds(1), Duration.ofSeconds(1));
    }

    private String payload() {
        return """
                {"location":{"name":"New Delhi","country":"India"},
                 "current":{"temp_c":28.5,"condition":{"text":"Sunny"},"humidity":60}}
                """;
    }
}
