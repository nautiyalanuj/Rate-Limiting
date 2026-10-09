//Copyright ANuj Nautiyal
package com.example.demo.observability;

import io.micrometer.observation.transport.SenderContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.stereotype.Component;

/** Preserve HTTP propagation while keeping provider bodies out of exception events. */
@Component
@Order(2000)
@ConditionalOnProperty(name = "app.tracing.enabled", havingValue = "true")
public class WeatherTracingHandler extends PropagatingSenderTracingObservationHandler<SenderContext<?>> {
    /**
     * @param tracer configured tracer
     * @param propagator configured W3C context propagation
     */
    public WeatherTracingHandler(Tracer tracer, Propagator propagator) {
        super(tracer, propagator);
    }

    /** @param context failed outgoing request; other clients retain standard error handling */
    @Override
    public void onError(SenderContext<?> context) {
        if (context instanceof ClientRequestObservationContext request
                && Boolean.TRUE.equals(request.getCarrier().getAttributes().get("weather.observation"))) {
            context.setError(new IllegalStateException("Weather request failed"));
        }
        super.onError(context);
    }
}
