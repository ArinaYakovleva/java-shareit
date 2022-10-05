package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDTOMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = repository.saveAndFlush(UserDTOMapper.fromUserDto(userDto));
        log.info(String.format("Добавление пользователя: %s", user));
        return UserDTOMapper.toUserDto(user);
    }

    @Override
    public UserDto editUser(UserDto user, Long id) {
        User existingUser = repository.findById(id).orElseThrow(() -> {
            String errorMessage = String.format("Пользователь с id=%d не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        });

        User userToUpdate = new User(
                id,
                user.getName() == null ? existingUser.getName() : user.getName(),
                user.getEmail() == null ? existingUser.getEmail() : user.getEmail()
        );
        log.info(String.format("Изменение пользователя: %s", userToUpdate));
        return UserDTOMapper.toUserDto(repository.saveAndFlush(userToUpdate));
    }

    @Override
    public UserDto getUser(Long id) {
        User user = repository.findById(id).orElseThrow(() -> {
            String errorMessage = String.format("Пользователь с id=%d не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        });
        return UserDTOMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return repository.findAll().stream()
                .map(UserDTOMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        log.info(String.format("Удаление пользователя с id=%d", id));
        repository.deleteById(id);
    }
}
