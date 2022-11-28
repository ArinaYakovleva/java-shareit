package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService service;

    @Autowired
    public RequestController(RequestService service) {
        this.service = service;
    }

    @PostMapping
    public RequestDto addRequest(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody CreateRequestDto requestDto) {
        return service.addRequest(requestDto, userId);
    }

    @GetMapping
    public List<RequestDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getOwnersRequests(userId);
    }

    @GetMapping("/all")
    public List<RequestDto> getAllRequests(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                           @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getAllRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public RequestDto getRequest(@PathVariable Long requestId,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getRequest(requestId, userId);
    }
}
