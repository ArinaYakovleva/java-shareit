package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private final User firstUser = new User(1L, "name", "name@mail.com");
    private final User secondUser = new User(1L, "name", "name@mail.com");

    @Test
    void testEquals() {
        assertEquals(firstUser, secondUser);
    }

    @Test
    void testHashCode() {
        assertEquals(firstUser.hashCode(), secondUser.hashCode());
    }
}
