package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {
    @Mock
    RequestService requestService;

    CreateRequestDto createRequestDto = new CreateRequestDto(1L, "нужна отвертка");

    RequestDto requestDto = new RequestDto(1L, "нужна отвертка",
            LocalDateTime.of(2022, 11, 11, 11, 0), new ArrayList<>());

    @Test
    void addRequest() {
        Mockito.when(requestService.addRequest(createRequestDto, 1L))
                .thenReturn(requestDto);
        RequestDto createdRequest = requestService.addRequest(createRequestDto, 1L);

        assertEquals(requestDto, createdRequest);
        Mockito.verify(requestService, Mockito.times(1))
                .addRequest(createRequestDto, 1L);
    }

    @Test
    void getOwnersRequest() {
        Mockito.when(requestService.getOwnersRequests(1L))
                .thenReturn(List.of(requestDto));
        List<RequestDto> ownersRequests = requestService.getOwnersRequests(1L);

        assertEquals(1, ownersRequests.size());
        assertEquals(requestDto, ownersRequests.get(0));
        Mockito.verify(requestService, Mockito.times(1))
                .getOwnersRequests(1L);
    }

    @Test
    void getAllRequests() {
        Mockito.when(requestService.getAllRequests(0, 10, 2L))
                .thenReturn(List.of());
        Mockito.when(requestService.getAllRequests(0, 10, 1L))
                .thenReturn(List.of(requestDto));

        List<RequestDto> emptyRequests = requestService.getAllRequests(0, 10, 2L);
        List<RequestDto> allRequests = requestService.getAllRequests(0, 10, 1L);

        assertEquals(0, emptyRequests.size());
        assertEquals(1, allRequests.size());
        assertEquals(requestDto, allRequests.get(0));

        Mockito.verify(requestService, Mockito.times(2))
                .getAllRequests(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong());
    }

    @Test
    void getRequest() {
        Mockito.when(requestService.getRequest(1L, 1L))
                .thenReturn(requestDto);

        RequestDto requestedDto = requestService.getRequest(1L, 1L);

        assertEquals(requestDto, requestedDto);
        Mockito.verify(requestService, Mockito.times(1))
                .getRequest(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong());
    }
}
