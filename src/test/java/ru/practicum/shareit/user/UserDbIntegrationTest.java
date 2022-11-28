package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.dto.UserDTOMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserDbIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    private UserDto user;
    private UserDto secondUser;

    @BeforeEach
    void init() {
        UserDto user = new UserDto(null, "test", "test@mail.ru");
        this.user = new UserDto(
                1L,
                "test",
                "test@mail.ru"
        );
        UserDto secondUser = new UserDto(null, "test2", "test2@mail.ru");
        this.secondUser = new UserDto(2L, "test2", "test2@mail.ru");
        userRepository.saveAndFlush(UserDTOMapper.fromUserDto(user));
        userRepository.saveAndFlush(UserDTOMapper.fromUserDto(secondUser));
    }

    @Test
    void editUser() {
        UserDto user = new UserDto(1L, "updated", "test5@mail.ru");
        User updatedUser = userRepository.saveAndFlush(UserDTOMapper.fromUserDto(user));

        Assertions.assertEquals(user, UserDTOMapper.toUserDto(updatedUser));
    }

    @Test
    void getUser() {
        UserDto userDto = UserDTOMapper.toUserDto(userRepository.findById(1L).get());
        Optional<User> userOptional = userRepository.findById(100L);

        Assertions.assertEquals(user, userDto);
        Assertions.assertFalse(userOptional.isPresent());
    }

    @Test
    void getAllUsers() {
        List<UserDto> userDtoList = userRepository.findAll().stream()
                .map(UserDTOMapper::toUserDto)
                .collect(Collectors.toList());
        Assertions.assertEquals(2, userDtoList.size());
    }

    @Test
    void deleteUser() {
        userRepository.deleteById(1L);
        List<UserDto> userDtoList = userRepository.findAll().stream()
                .map(UserDTOMapper::toUserDto)
                .collect(Collectors.toList());
        Assertions.assertEquals(1, userDtoList.size());
        Assertions.assertEquals(secondUser, userDtoList.get(0));
    }
}
