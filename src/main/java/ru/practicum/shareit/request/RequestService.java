package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto addRequest(CreateRequestDto requestDto, Long userId);

    List<RequestDto> getOwnersRequests(Long userId);

    List<RequestDto> getAllRequests(Integer from, Integer to, Long userId);

    RequestDto getRequest(Long id, Long userId);
}
