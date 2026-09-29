//Copyright ANuj Nautiyal
package com.example.demo.controller;

import com.example.demo.dto.User;
import com.example.demo.dto.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserListController {
    private final UserRepository userRepository;

    @GetMapping("")
    public List<User> listAll() {
        return userRepository.findAll();
    }
}
