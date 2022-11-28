package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDTOMapper;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public RequestDto addRequest(CreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id=%d не найден", userId);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });

        Request request = requestRepository.saveAndFlush(
                RequestDTOMapper.fromCreateRequestDto(requestDto, user, new ArrayList<>(), LocalDateTime.now()));
        List<Item> items = itemRepository.findAllByRequest_Id(request.getId());
        log.info(String.format("Добавление запроса: %s", requestDto));
        return RequestDTOMapper.toRequestDto(request, items);
    }

    @Override
    public List<RequestDto> getOwnersRequests(Long userId) {
        Sort sort = Sort.by("created").descending();
        if (userRepository.findById(userId).isEmpty()) {
            String errorMessage = String.format("Пользователь с id=%d не найден", userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return requestRepository
                .findAllByRequestor_Id(userId, sort)
                .stream()
                .map(this::getRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getAllRequests(Integer from, Integer to, Long userId) {
        CustomPageRequest pageRequest = new CustomPageRequest(from, to, Sort.by("created").descending());

        return requestRepository.findAllByRequestor_IdNot(userId, pageRequest)
                .stream()
                .map(this::getRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestDto getRequest(Long id, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            String errorMessage = String.format("Пользователь с id=%d не найден", userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Запрос с id=%d не найден", id);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });
        return getRequestDto(request);
    }

    private RequestDto getRequestDto(Request request) {
        List<Item> items = itemRepository.findAllByRequest_Id(request.getId());
        return RequestDTOMapper.toRequestDto(request, items);
    }
}
