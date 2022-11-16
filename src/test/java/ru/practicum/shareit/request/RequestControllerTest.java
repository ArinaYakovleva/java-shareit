package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RequestControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    CreateRequestDto createRequestDto;
    CreateRequestDto secondCreateRequestDto;
    ItemDto itemDto;

    @BeforeEach
    void init() throws Exception {
        UserDto userDto = new UserDto(1L, "test user", "test@mail.ru");
        UserDto secondUser = new UserDto(2L, "Ivan", "ivan@mail.ru");

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(secondUser))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        createRequestDto = new CreateRequestDto(1L, "нужна дрель");
        secondCreateRequestDto = new CreateRequestDto(2L, "нужна отвертка");

        mockMvc.perform(post("/requests")
                .content(objectMapper.writeValueAsString(createRequestDto))
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/requests")
                .content(objectMapper.writeValueAsString(secondCreateRequestDto))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        itemDto = new ItemDto(1L, "дрель", "test дрель", true, 1L);

        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(itemDto))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void addRequest() throws Exception {
        CreateRequestDto newRequest = new CreateRequestDto(3L, "нужна машина");

        mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(newRequest))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.description", is(newRequest.getDescription())))
                .andExpect(jsonPath("$.items", hasSize(0)));

    }

    @Test
    void addRequestBlankDescription() throws Exception {
        CreateRequestDto newRequest = new CreateRequestDto(3L, "");

        mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(newRequest))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addRequestNullDescription() throws Exception {
        CreateRequestDto newRequest = new CreateRequestDto(3L, null);

        mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(newRequest))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addRequestUnknownUser() throws Exception {
        CreateRequestDto newRequest = new CreateRequestDto(3L, "test");

        mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(newRequest))
                        .header("X-Sharer-User-Id", 3)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnersRequests() throws Exception {
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(createRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(createRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(itemDto.getId()), Long.class));
    }

    @Test
    void getOwnersRequestsWrongUser() throws Exception {
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 3)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRequests() throws Exception {
        mockMvc.perform(get("/requests/all?from=0&size=10")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(secondCreateRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(secondCreateRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].items", hasSize(0)));
    }

    @Test
    void getRequest() throws Exception {
        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(createRequestDto.getDescription())));
    }

    @Test
    void getNotFoundRequest() throws Exception {
        mockMvc.perform(get("/requests/3")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestWrongUser() throws Exception {
        mockMvc.perform(get("/requests/2")
                        .header("X-Sharer-User-Id", 3)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
