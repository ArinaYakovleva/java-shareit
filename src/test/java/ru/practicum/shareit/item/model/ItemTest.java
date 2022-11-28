package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {
    private final User user = new User(1L, "name", "name@mail.com");
    private final LocalDateTime date = LocalDateTime.of(2022, 12, 12, 11, 0);

    private final Request request = new Request(1L, "test", user, date);

    private final Item firstItem = new Item(1L, "test", "test", user, true, request);

    private final Item secondItem = new Item(1L, "test", "test", user, true, request);

    @Test
    void testEquals() {
        assertEquals(firstItem, secondItem);
    }

    @Test
    void testHashCode() {
        assertEquals(firstItem.hashCode(), secondItem.hashCode());
    }
}
