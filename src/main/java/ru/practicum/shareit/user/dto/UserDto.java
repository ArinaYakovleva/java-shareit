package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class UserDto {
    private final Long id;

    @NotNull(message = "Имя не может равняться null")
    private final String name;

    @Email(message = "Некорректный email")
    @NotNull(message = "Email не может равняться null")
    private final String email;
}
