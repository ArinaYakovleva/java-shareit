package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {
    @InjectMocks
    private RequestServiceImpl requestService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RequestRepository requestRepository;

    private final User user = new User(1L, "test", "test@mail.ru");

    private final Request request = new Request(1L, "нужна отвертка", user,
            LocalDateTime.of(2022, 11, 11, 11, 0));
    private final CreateRequestDto createRequestDto = new CreateRequestDto(1L, "нужна отвертка");

    private final RequestDto requestDto = new RequestDto(1L, "нужна отвертка",
            LocalDateTime.of(2022, 11, 11, 11, 0), new ArrayList<>());

    @Test
    void addRequest() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.saveAndFlush(request))
                .thenReturn(request);
        Mockito.when(itemRepository.findAllByRequest_Id(1L))
                .thenReturn(List.of());

        RequestDto createdRequest = requestService.addRequest(createRequestDto, 1L);

        assertEquals(requestDto, createdRequest);
        Mockito.verify(requestRepository, Mockito.times(1))
                .saveAndFlush(request);
    }

    @Test
    void addRequestUnknownUser() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> requestService.addRequest(createRequestDto, 1L));

        assertEquals("Пользователь с id=1 не найден", notFoundException.getMessage());
    }

    @Test
    void getOwnersRequest() {
        Sort sort = Sort.by("created").descending();
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findAllByRequestor_Id(1L, sort))
                .thenReturn(List.of(request));

        List<RequestDto> ownersRequests = requestService.getOwnersRequests(1L);

        assertEquals(1, ownersRequests.size());
        assertEquals(requestDto, ownersRequests.get(0));
        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByRequestor_Id(1L, sort);
    }

    @Test
    void getOwnersRequestsUnknownUser() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> requestService.getOwnersRequests(2L));

        assertEquals("Пользователь с id=2 не найден", notFoundException.getMessage());
    }
    @Test
    void getAllRequests() {
        CustomPageRequest pageRequest = new CustomPageRequest(0, 10, Sort.by("created").descending());

        Mockito.when(requestRepository.findAllByRequestor_IdNot(1L, pageRequest))
                .thenReturn(List.of());

        List<RequestDto> emptyRequestDtos = requestService.getAllRequests(0, 10, 1L);
        assertEquals(0, emptyRequestDtos.size());

        Mockito.when(requestRepository.findAllByRequestor_IdNot(2L, pageRequest))
                .thenReturn(List.of(request));

        List<RequestDto> requestDtos = requestService.getAllRequests(0, 10, 2L);
        assertEquals(1, requestDtos.size());
        assertEquals(requestDto, requestDtos.get(0));
    }

    @Test
    void getRequest() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        RequestDto requestedDto = requestService.getRequest(1L, 1L);

        assertEquals(requestDto, requestedDto);

    }

    @Test
    void getUnknownRequest() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(2L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> requestService.getRequest(2L, 1L));

        assertEquals("Запрос с id=2 не найден", notFoundException.getMessage());
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(anyLong());
    }

    @Test
    void getUnknownUserRequest() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> requestService.getRequest(1L, 1L));

        assertEquals("Пользователь с id=1 не найден", notFoundException.getMessage());
    }
}
