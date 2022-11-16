package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    UserDto user;
    UserDto secondUser;

    @BeforeEach
    void init() throws Exception {
        user = new UserDto(1L, "test", "test@mail.ru");
        secondUser = new UserDto(2L, "test 2", "test2@mail.ru");
        performPost(user);
        performPost(secondUser);
    }

    @Test
    void addUserOk() throws Exception {
        UserDto userToCreate = new UserDto(3L, "new test", "new@mail.ru");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userToCreate.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userToCreate.getName())))
                .andExpect(jsonPath("$.email", is(userToCreate.getEmail())));
    }

    @Test
    void addUserNoName() throws Exception {
        UserDto userToCreate = new UserDto(3L, null, "new@mail.ru");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addUserNoEmail() throws Exception {
        UserDto userToCreate = new UserDto(3L, "test", null);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));

    }

    @Test
    void addUserDuplicateEmail() throws Exception {
        UserDto userToCreate = new UserDto(3L, "new user", "test@mail.ru");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500));
    }

    @Test
    void editUser() throws Exception {
        UserDto userToUpdate = new UserDto(1L, "updated name", "updated@mail.com");
        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(userToUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userToUpdate.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userToUpdate.getName())))
                .andExpect(jsonPath("$.email", is(userToUpdate.getEmail())));
    }

    @Test
    void editUnknownUser() throws Exception {
        UserDto userToUpdate = new UserDto(4L, "updated name", "updated@mail.com");
        mockMvc.perform(patch("/users/4")
                        .content(objectMapper.writeValueAsString(userToUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void editWithDuplicateEmail() throws Exception {
        UserDto userToUpdate = new UserDto(1L, "updated name", "test2@mail.ru");
        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(userToUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500));
    }

    @Test
    void getUser() throws Exception {
        mockMvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void getNotFoundUser() throws Exception {
        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200));
    }

    @Test
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(secondUser.getId()), Long.class));
    }

    private void performPost(UserDto userDto) throws Exception {
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
