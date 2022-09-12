package ru.practicum.shareit.item;

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
class ItemControllerTest {
    private final String itemTemplate = "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"available\":%b}";

    private String firstItem;
    private String secondItem;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() throws Exception {
        String userTemplate = "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}";
        String user1 = String.format(userTemplate, 1, "Иван", "ivan@mail.ru");
        String user2 = String.format(userTemplate, 2, "Александр", "alex@mail.ru");

        performUsersPost(user1);
        performUsersPost(user2);

        this.firstItem = String.format(itemTemplate, 1, "Отвертка", "лучшая отвертка", true);
        this.secondItem = String.format(itemTemplate, 2, "Дрель", "лучшая дрель", true);

        performItemsPost(firstItem, 1);
        performItemsPost(secondItem, 2);
    }

    @Test
    public void getAllItemsOfFirstUser() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andReturn();
        String expectedResult = "[" + firstItem + "]";
        Assertions.assertEquals(expectedResult, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

    }

    @Test
    public void getAllItemsOfSecondUser() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andReturn();
        String expectedResult = "[" + secondItem + "]";
        Assertions.assertEquals(expectedResult, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

    }

    @Test
    public void getAllItemsWithoutHeader() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getItem() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk()).andReturn();
        Assertions.assertEquals(firstItem, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void getItemIfNotFound() throws Exception {
        mockMvc.perform(get("/items/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void searchItem() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/items/search?text=оТверт"))
                .andExpect(status().isOk())
                .andReturn();
        String expectedResult = "[" + firstItem + "]";
        Assertions.assertEquals(expectedResult, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void searchItemNoResult() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/items/search?text="))
                .andExpect(status().isOk())
                .andReturn();
        String expectedResult = "[]";
        Assertions.assertEquals(expectedResult, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void addItem() throws Exception {
        String item = String.format(itemTemplate, 3, "test", "test test", true);
        MvcResult mvcResult = performItemsPost(item, 2);
        Assertions.assertEquals(item, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void addItemWithoutName() throws Exception {
        String item = "{\"description\":\"test\",\"available\":true}";
        mockMvc.perform(post("/items")
                        .content(item)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addWithNotFoundUser() throws Exception {
        String item = String.format(itemTemplate, 3, "test", "test test", true);
        mockMvc.perform(post("/items")
                        .content(item)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 3))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addItemWithoutDescription() throws Exception {
        String item = "{\"name\":\"item\",\"available\":true}";
        mockMvc.perform(post("/items")
                        .content(item)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithoutAvailable() throws Exception {
        String item = "{\"name\":\"item\",\"description\":\"test\"}";
        mockMvc.perform(post("/items")
                        .content(item)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithoutHeader() throws Exception {
        String item = "{\"name\":\"item\",\"description\":\"test\",\"available\":true}";
        mockMvc.perform(post("/items")
                        .content(item)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void editItem() throws Exception {
        String item = "{\"id\":1,\"name\":\"вещь\",\"description\":\"описание\",\"available\":false}";
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .content(item)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals(item, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void editItemAccessDenied() throws Exception {
        String item = "{\"id\":1,\"name\":\"вещь\",\"description\":\"описание\",\"available\":false}";
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2)
                        .content(item)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void editItemNotFound() throws Exception {
        mockMvc.perform(patch("/items/3")
                        .header("X-Sharer-User-Id", 2)
                        .content(firstItem)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void editName() throws Exception {
        String nameJson = "{\"name\": \"new name\"}";
        String expectedResult = "{\"id\":1,\"name\":\"new name\",\"description\":\"лучшая отвертка\",\"available\":true}";

        editTest(nameJson, expectedResult);
    }

    @Test
    public void editDescription() throws Exception {
        String nameJson = "{\"description\": \"new description\"}";
        String expectedResult = "{\"id\":1,\"name\":\"Отвертка\",\"description\":\"new description\",\"available\":true}";

        editTest(nameJson, expectedResult);
    }

    @Test
    public void editAvailable() throws Exception {
        String nameJson = "{\"available\":false}";
        String expectedResult = "{\"id\":1,\"name\":\"Отвертка\",\"description\":\"лучшая отвертка\",\"available\":false}";

        editTest(nameJson, expectedResult);
    }

    @Test
    public void deleteItem() throws Exception {
        String expectedResult = "[]";

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(expectedResult, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void deleteItemAccessDenied() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteNotFoundItem() throws Exception {
        mockMvc.perform(delete("/items/3")
                        .header("X-Sharer-User-Id", 3))
                .andExpect(status().isNotFound());
    }

    private void performUsersPost(String content) throws Exception {
        mockMvc.perform(post("/users")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private MvcResult performItemsPost(String content, Integer ownerId) throws Exception {
        return mockMvc.perform(post("/items")
                        .content(content)
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private void editTest(String json, String expectedResult) throws Exception {
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(expectedResult, mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
