//Copyright ANuj Nautiyal
package com.example.demo.service;

import com.example.demo.dto.User;
import com.example.demo.dto.UserRepository;
import com.example.demo.resilience.BreakerProperties;
import com.example.demo.resilience.Breakers;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTests {
    private final UserRepository repository = mock(UserRepository.class);
    private final Breakers breakers = new Breakers(new BreakerProperties(2, 2, 50, Duration.ofSeconds(30)));
    private final UserService service = new UserService(repository, breakers);

    @Test
    void readsUsersThroughDatabaseCircuit() {
        User user = new User(UUID.randomUUID(), "Ada");
        when(repository.findAll()).thenReturn(List.of(user));
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThat(service.listAll()).containsExactly(user);
        assertThat(service.find(user.getId())).contains(user);
    }

    @Test
    void bothReadMethodsShareDatabaseFailuresAndStopRepositoryCalls() {
        when(repository.findAll()).thenThrow(new DataAccessResourceFailureException("offline"));
        for (int i = 0; i < 2; i++) assertThatThrownBy(service::listAll)
                .isInstanceOf(DataAccessResourceFailureException.class);
        assertThatThrownBy(() -> service.find(UUID.randomUUID())).isInstanceOf(CallNotPermittedException.class);
        verify(repository, times(2)).findAll();
        verify(repository, never()).findById(any());
    }
}
