package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserServiceImpl service;

    @Autowired
    public UserController(UserServiceImpl service) {
        this.service = service;
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return service.getUser(id);
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto user) {
        return service.addUser(user);
    }

    @PatchMapping("/{id}")
    public UserDto editUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return service.editUser(userDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        service.deleteUser(id);
    }
}
