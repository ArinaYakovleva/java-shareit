package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.ArrayList;

@JsonTest
class ItemJsonTest {
    @Autowired
    private JacksonTester<ItemDto> itemDtoJson;

    @Autowired
    private JacksonTester<ItemBookingDto> itemBookingDtoJson;

    @Autowired
    private JacksonTester<CommentDto> commentDtoJson;

    @Test
    void testItemDtoJson() throws Exception {
        ItemDto itemDto = new ItemDto(
                1L,
                "Отвертка",
                "отличная отвертка",
                true,
                null
        );

        JsonContent<ItemDto> result = itemDtoJson.write(itemDto);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo("Отвертка");
        Assertions.assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("отличная отвертка");
        Assertions.assertThat(result).extractingJsonPathBooleanValue("$.available")
                .isEqualTo(true);
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.requestId")
                .isNull();
    }

    @Test
    void testItemBookingDto() throws Exception {
        ItemBookingDto itemBookingDto = new ItemBookingDto(
                1L,
                "Отвертка",
                "лучшая отвертка",
                true,
                null,
                null,
                new ArrayList<>());

        JsonContent<ItemBookingDto> result = itemBookingDtoJson.write(itemBookingDto);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo("Отвертка");
        Assertions.assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("лучшая отвертка");
        Assertions.assertThat(result).extractingJsonPathBooleanValue("$.available")
                .isEqualTo(true);
        Assertions.assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        Assertions.assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
        Assertions.assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();
    }

    @Test
    void testCommentDto() throws Exception {
        CommentDto commentDto = new CommentDto(1L, "нужна отвертка", "Иван",
                LocalDateTime.of(2022, 10, 5, 10, 0));


        JsonContent<CommentDto> result = commentDtoJson.write(commentDto);

        System.out.println(result.toString());
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo("нужна отвертка");
        Assertions.assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2022-10-05T10:00:00");
    }
}
