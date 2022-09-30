package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {
    private final String template = "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}";
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() throws Exception {
        String templateStr = "{\"name\":\"%s\",\"email\":\"%s\"}";
        String user1 = String.format(templateStr, "Иван", "ivan@mail.ru");
        String user2 = String.format(templateStr, "Александр", "alex@mail.ru");

        mockMvc.perform(post("/users")
                .content(user1)
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(post("/users")
                .content(user2)
                .contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    public void getAllUsers() throws Exception {
        String template = "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}";
        String user1 = String.format(template, 1, "Иван", "ivan@mail.ru");
        String user2 = String.format(template, 2, "Александр", "alex@mail.ru");
        String resultStr = "[" + user1 + "," + user2 + "]";

        MvcResult mvcResult = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals(resultStr, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void getUser() throws Exception {
        String user = String.format(template, 1, "Иван", "ivan@mail.ru");
        MvcResult mvcResult = mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals(user, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void getUnknownUser() throws Exception {
        mockMvc.perform(get("/users/3"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void addUser() throws Exception {
        String user = String.format(template, 3, "Test", "test@mail.ru");
        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(user, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void addInvalidUser() throws Exception {
        String user = String.format("{\"name\": \"%s\"}", "test");
        mockMvc.perform(post("/users")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addIncorrectEmail() throws Exception {
        String user = String.format("{\"email\": \"%s\"}", "test@");
        mockMvc.perform(post("/users")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addExistingEmail() throws Exception {
        String user = String.format("{\"email\": \"%s\"}", "ivan@mail.ru");
        mockMvc.perform(post("/users")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void editUser() throws Exception {
        String user = String.format(template, 2, "Updated", "updated@mail.ru");

        editTest(user, user);
    }

    @Test
    public void editUserWithExistingEmail() throws Exception {
        String emailJson = "{\"email\":\"ivan@mail.ru\"}";
        mockMvc.perform(patch("/users/2")
                        .content(emailJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void editUserName() throws Exception {
        String nameJson = "{\"name\":\"Updated\"}";
        String expectedResult = "{\"id\":2,\"name\":\"Updated\",\"email\":\"alex@mail.ru\"}";

        editTest(nameJson, expectedResult);
    }

    @Test
    public void editUserEmail() throws Exception {
        String emailJson = "{\"email\":\"a@mail.ru\"}";
        String expectedResult = "{\"id\":2,\"name\":\"Александр\",\"email\":\"a@mail.ru\"}";

        editTest(emailJson, expectedResult);
    }

    @Test
    public void editUnknownUser() throws Exception {
        String user = String.format(template, 3, "Updated", "updated@mail.ru");

        mockMvc.perform(patch("/users/3")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        String expectedResult = "[{\"id\":2,\"name\":\"Александр\",\"email\":\"alex@mail.ru\"}]";

        MvcResult mvcResult = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(expectedResult, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void deleteNotFoundUser() throws Exception {
        mockMvc.perform(delete("/users/3"))
                .andExpect(status().isNotFound());
    }

    private void editTest(String content, String expectedResult) throws Exception {
        mockMvc.perform(patch("/users/2")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(expectedResult, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
