package ru.practicum.shareit.request.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {
    private final User user = new User(1L, "name", "name@mail.com");
    private final LocalDateTime date = LocalDateTime.of(2022, 12, 12, 11, 0);

    private final Request firstRequest = new Request(1L, "test", user, date);

    private final Request secondRequest = new Request(1L, "test", user, date);

    @Test
    void testEquals() {
        assertEquals(firstRequest, secondRequest);
    }

    @Test
    void testHashCode() {
        assertEquals(firstRequest.hashCode(), secondRequest.hashCode());
    }
}
