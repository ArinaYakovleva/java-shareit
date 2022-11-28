package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    private final User user = new User(
            1L,
            "test user",
            "test@mail.com"
    );

    private final UserDto userDto = new UserDto(
            1L,
            "test user",
            "test@mail.com"
    );

    @Test
    void addUser() {
        Mockito.when(userRepository.saveAndFlush(user))
                .thenReturn(user);

        UserDto createdUser = userService.addUser(userDto);
        assertEquals(userDto, createdUser);
        Mockito.verify(userRepository, Mockito.times(1))
                .saveAndFlush(user);

    }

    @Test
    void editUser() {
        User userToUpdate = new User(1L, "updated", "test@mail.ru");
        UserDto userToUpdateDto = new UserDto(1L, "updated", "test@mail.ru");

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.saveAndFlush(userToUpdate))
                .thenReturn(userToUpdate);

        UserDto updatedUser = userService.editUser(userToUpdateDto, 1L);

        assertEquals(userToUpdateDto, updatedUser);
        Mockito.verify(userRepository, Mockito.times(1))
                .saveAndFlush(userToUpdate);
    }

    @Test
    void editNotFoundUser() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.editUser(userDto, 2L));

        assertEquals("Пользователь с id=2 не найден", notFoundException.getMessage());
    }
    @Test
    void getUser() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDto foundUser = userService.getUser(1L);
        assertEquals(userDto, foundUser);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);

        Mockito.when(userRepository.findById(2L))
                .thenThrow(new NotFoundException("Пользователь с id=2 не найден"));

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.getUser(2L));

        assertEquals("Пользователь с id=2 не найден", notFoundException.getMessage());
    }

    @Test
    void getNotFoundUser() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.getUser(2L));

        assertEquals("Пользователь с id=2 не найден", notFoundException.getMessage());
    }

    @Test
    void getAllUsers() {
        Mockito.when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<UserDto> foundUsers = userService.getAllUsers();
        assertEquals(1, foundUsers.size());
        assertEquals(userDto, foundUsers.get(0));
        Mockito.verify(userRepository, Mockito.times(1))
                .findAll();

    }

    @Test
    void deleteUser() {
        userService.deleteUser(1L);
        Mockito.verify(userRepository, Mockito.times(1))
                .deleteById(1L);
    }
}
