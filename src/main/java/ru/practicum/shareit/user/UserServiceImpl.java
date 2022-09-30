package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements BaseUserService {
    private final UserStorage storage;

    @Autowired
    public UserServiceImpl(UserStorage storage) {
        this.storage = storage;
    }

    @Override
    public UserDto addUser(UserDto user) {
        UserDto userDto = storage.addUser(user);
        log.info(String.format("Добавление пользователя: %s", user));
        return userDto;
    }

    @Override
    public UserDto editUser(UserDto user, Long id) {
        UserDto userDto = storage.editUser(user, id);
        if (userDto == null) {
            String errorMessage = String.format("Пользователь с id=%d не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        log.info(String.format("Изменение пользователя с id=%d", id));
        return userDto;
    }

    @Override
    public UserDto getUser(Long id) {
        UserDto userDto = storage.getUser(id);
        if (userDto == null) {
            String errorMessage = String.format("Пользователь с id=%d не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return userDto;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return storage.getAllUsers();
    }

    @Override
    public void deleteUser(Long id) {
        storage.deleteUser(id);
        log.info(String.format("Удаление пользователя с id=%d", id));
    }
}
