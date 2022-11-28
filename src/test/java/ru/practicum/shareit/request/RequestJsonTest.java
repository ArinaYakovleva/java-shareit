package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;

import java.time.LocalDateTime;
import java.util.List;

@JsonTest
class RequestJsonTest {
    @Autowired
    private JacksonTester<CreateRequestDto> createRequestDtoJson;

    @Autowired
    private JacksonTester<RequestDto> requestDtoJson;

    @Test
    void testCreateRequestDto() throws Exception {
        CreateRequestDto createRequestDto = new CreateRequestDto(1L, "нужна дрель");

        JsonContent<CreateRequestDto> result = createRequestDtoJson.write(createRequestDto);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(createRequestDto.getDescription());
    }

    @Test
    void testRequestDto() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "дрель", "дрель", true, 1L);

        RequestDto requestDto = new RequestDto(1L, "нужна дрель",
                LocalDateTime.of(2022, 11, 11, 11, 0), List.of(itemDto));

        JsonContent<RequestDto> result = requestDtoJson.write(requestDto);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(requestDto.getDescription());
        Assertions.assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2022-11-11T11:00:00");
        Assertions.assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2022-11-11T11:00:00");
        Assertions.assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.items[0].id")
                .isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId")
                .isEqualTo(1);
    }
}
