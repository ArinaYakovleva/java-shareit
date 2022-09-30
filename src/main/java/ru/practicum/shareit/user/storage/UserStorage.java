package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.exception.exceptions.UniquenessConflictException;
import ru.practicum.shareit.user.BaseUserService;
import ru.practicum.shareit.user.dto.UserDTOMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserStorage implements BaseUserService {
    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 0L;

    @Override
    public UserDto addUser(UserDto user) {
        idCounter++;
        UserDto userToCreate = new UserDto(
                idCounter,
                user.getName(),
                user.getEmail()
        );
        if (validateEmail(userToCreate)) {
            idCounter--;
            String errorMessage = String.format("Пользователь с email=%s уже существует", user.getEmail());
            log.error(errorMessage);
            throw new UniquenessConflictException(errorMessage);
        }

        users.put(idCounter, UserDTOMapper.fromUserDto(userToCreate));
        return userToCreate;
    }

    @Override
    public UserDto editUser(UserDto user, Long id) {
        User existingUser = users.get(id);
        if (existingUser == null) {
            return null;
        }
        UserDto userToUpdate = new UserDto(
                id,
                user.getName() == null ? existingUser.getName() : user.getName(),
                user.getEmail() == null ? existingUser.getEmail() : user.getEmail()
        );

        if (validateEmail(userToUpdate)) {
            String errorMessage = String.format("Пользователь с email=%s уже существует", user.getEmail());
            log.error(errorMessage);
            throw new UniquenessConflictException(errorMessage);
        }

        users.put(id, UserDTOMapper.fromUserDto(userToUpdate));
        return userToUpdate;
    }

    @Override
    public UserDto getUser(Long id) {
        return UserDTOMapper.toUserDto(users.get(id));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserDTOMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        User user = users.get(id);
        if (user == null) {
            String errorMessage = String.format("Пользователь с id=%d не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        users.remove(id);
    }

    private boolean validateEmail(UserDto userDto) {
        return users.values().stream()
                .anyMatch(user -> Objects.equals(user.getEmail(),
                        userDto.getEmail()) && !user.getId().equals(userDto.getId()));
    }
}
