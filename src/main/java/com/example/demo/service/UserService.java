//Copyright ANuj Nautiyal
package com.example.demo.service;

import com.example.demo.dto.User;
import com.example.demo.dto.UserRepository;
import com.example.demo.resilience.Breakers;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Both controllers share one database breaker because they use one dependency. */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final Breakers breakers;

    /** @return users; repository methods manage their read-only transactions */
    public List<User> listAll() {
        return breakers.execute("database", repository::findAll);
    }

    /**
     * @param id user identifier
     * @return a match or an empty result
     */
    public Optional<User> find(UUID id) {
        return breakers.execute("database", () -> repository.findById(id));
    }
}
