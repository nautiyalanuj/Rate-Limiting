//Copyright ANuj Nautiyal
package com.example.demo.controller;

import com.example.demo.dto.User;
import com.example.demo.service.UserService;
import com.example.demo.annotations.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/** User reads share the users quota and database circuit breaker. */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserDetailController {
    private final UserService users;

    /** @return repository data after the shared quota admits this request */
    @GetMapping("/{id}")
    @RateLimiter("users")
    public Optional<User> getById(@PathVariable UUID id) {
        return users.find(id);
    }
}
