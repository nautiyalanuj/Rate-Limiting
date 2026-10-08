//Copyright ANuj Nautiyal
package com.example.demo.limiting;

import com.example.demo.annotations.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** Enforce the custom annotation before MVC calls a protected controller. */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitRegistry registry;

    // MVC invokes this before the controller, so rejections never reach a dependency.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod()) || !(handler instanceof HandlerMethod method)) return true;
        RateLimiter annotation = method.getMethodAnnotation(RateLimiter.class);
        if (annotation != null) enforce(annotation.value());
        return true;
    }

    private void enforce(String name) {
        LimitDecision decision = registry.acquire(name);
        if (!decision.allowed()) throw new RateLimitExceeded(name, decision);
    }
}
