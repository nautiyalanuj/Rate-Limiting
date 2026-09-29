//Copyright ANuj Nautiyal
package com.example.demo.controller;

import com.example.demo.dto.User;
import com.example.demo.dto.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserDetailController {
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public Optional<User> getById(@PathVariable UUID id) {
        return userRepository.findById(id);
    }
}
