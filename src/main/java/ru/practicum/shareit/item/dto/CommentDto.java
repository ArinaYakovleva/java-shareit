package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private final Long id;
    @NotBlank
    private final String text;
    private final String authorName;
    private final LocalDateTime created;
}
