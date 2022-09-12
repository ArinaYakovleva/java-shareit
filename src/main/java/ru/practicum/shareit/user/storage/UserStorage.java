package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.BaseUserService;
import ru.practicum.shareit.user.model.User;

public interface UserStorage extends BaseUserService {
    User deleteUser(Long id);
}
