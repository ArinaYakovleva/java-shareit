package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

public abstract class UserDTOMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
    public static User fromUserDto(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }
}
