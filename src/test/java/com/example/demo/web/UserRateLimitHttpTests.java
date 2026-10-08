//Copyright ANuj Nautiyal
package com.example.demo.web;

import com.example.demo.api.ApiExceptionHandler;
import com.example.demo.controller.UserDetailController;
import com.example.demo.controller.UserListController;
import com.example.demo.dto.UserRepository;
import com.example.demo.limiting.*;
import com.example.demo.resilience.*;
import com.example.demo.service.UserService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserRateLimitHttpTests {
    private final UserRepository repository = mock(UserRepository.class);

    @Test
    void listAndDetailControllersSpendTheSameQuotaBeforeRepositoryCalls() throws Exception {
        when(repository.findAll()).thenReturn(List.of());
        MockMvc mvc = mvc();
        mvc.perform(get("/users")).andExpect(status().isOk());
        mvc.perform(get("/users/00000000-0000-0000-0000-000000000001")).andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", matchesPattern("[1-9][0-9]*")));
        verify(repository, times(1)).findAll();
        verify(repository, never()).findById(any());
    }

    private MockMvc mvc() {
        Breakers breakers = new Breakers(new BreakerProperties(2, 2, 50, Duration.ofSeconds(5)));
        UserService users = new UserService(repository, breakers);
        return MockMvcBuilders.standaloneSetup(new UserListController(users), new UserDetailController(users))
                .addInterceptors(new RateLimitInterceptor(registry())).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    private RateLimitRegistry registry() {
        RateLimitPolicy policy = new RateLimitPolicy(Algorithm.FIXED_WINDOW, 1, Duration.ofDays(1));
        return new RateLimitRegistry(new RateLimitProperties(Map.of("users", policy)));
    }
}
