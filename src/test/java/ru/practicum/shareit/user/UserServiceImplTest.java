package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    UserService userService;
    UserDto user;
    UserDto userWithId;

    @BeforeEach
    public void init() {
        user = new UserDto(
                null,
                "test user",
                "test@mail.com"
        );

        userWithId = new UserDto(
                1L,
                "new user",
                "new@mail.ro"
        );
    }

    @Test
    void addUser() {
        UserDto testUser = new UserDto(
                1L,
                "test user",
                "test@mail.com"
        );
        Mockito.when(userService.addUser(user))
                .thenReturn(testUser);

        UserDto createdUser = userService.addUser(user);
        assertEquals(testUser, createdUser);
        Mockito.verify(userService, Mockito.times(1)).addUser(user);

    }

    @Test
    void editUser() {
        Mockito.when(userService.editUser(any(UserDto.class), anyLong()))
                .thenReturn(userWithId);
        UserDto updatedUser = userService.editUser(userWithId, 1L);

        assertEquals(userWithId, updatedUser);
        Mockito.verify(userService, Mockito.times(1))
                .editUser(userWithId, 1L);
    }

    @Test
    void getUser() {
        Mockito.when(userService.getUser(1L))
                .thenReturn(userWithId);
        UserDto foundUser = userService.getUser(1L);
        assertEquals(userWithId, foundUser);

        Mockito.verify(userService, Mockito.times(1))
                .getUser(1L);
    }

    @Test
    void getAllUsers() {
        List<UserDto> users = getUsers();
        Mockito.when(userService.getAllUsers())
                .thenReturn(users);

        List<UserDto> foundUsers = userService.getAllUsers();
        assertEquals(3, foundUsers.size());
        Mockito.verify(userService, Mockito.times(1))
                .getAllUsers();

    }

    @Test
    void deleteUser() {
        userService.deleteUser(1L);
        Mockito.verify(userService, Mockito.times(1))
                .deleteUser(1L);
    }


    private List<UserDto> getUsers() {
        List<UserDto> userDtos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            userDtos.add(new UserDto(
                    (long) i + 1,
                    "test",
                    "test@mail.ru"
            ));
        }
        return userDtos;
    }
}
