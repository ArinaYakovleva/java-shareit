package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

@JsonTest
public class UserJsonTest {
    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testUserJson() throws Exception {
        UserDto userDto = new UserDto(
                1L,
                "test",
                "test@mail.ru"
        );

        JsonContent<UserDto> result = json.write(userDto);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("test");
        Assertions.assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("test@mail.ru");
    }
}
