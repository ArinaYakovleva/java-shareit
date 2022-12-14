package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto user);

    UserDto editUser(UserDto user, Long id);

    UserDto getUser(Long id);

    List<UserDto> getAllUsers();

    void deleteUser(Long id);
}
