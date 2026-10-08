//Copyright ANuj Nautiyal
package com.example.demo.controller;

import com.example.demo.dto.User;
import com.example.demo.service.UserService;
import com.example.demo.annotations.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** User reads share the users quota and database circuit breaker. */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserListController {
    private final UserService users;

    /** @return repository data after the shared quota admits this request */
    @GetMapping("")
    @RateLimiter("users")
    public List<User> listAll() {
        return users.listAll();
    }
}
