//Copyright ANuj Nautiyal
package com.example.demo.config;

import io.micrometer.common.KeyValues;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;

/** Weather credentials live in the query string; never export the resolved URL. */
public class WeatherObservationConvention extends DefaultClientRequestObservationConvention {
    /**
     * @param context outgoing HTTP request
     * @return no resolved URL or query values
     */
    @Override
    public KeyValues getHighCardinalityKeyValues(ClientRequestObservationContext context) {
        return KeyValues.empty();
    }
}
