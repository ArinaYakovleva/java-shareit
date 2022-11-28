package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.ForbiddenException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.RequestRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RequestRepository requestRepository;

    private final User user = new User(1L, "test", "test@mail.ru");
    private final User secondUser = new User(1L, "test", "test@mail.ru");

    private final Item item = new Item(
            1L,
            "Отвертка",
            "лучшая отвертка",
            user, true,
            null
    );

    private final ItemDto itemDto = new ItemDto(
            null,
            "Отвертка",
            "лучшая отвертка",
            true,
            null
    );
    private final ItemDto itemDtoWithId = new ItemDto(
            1L,
            "Отвертка",
            "лучшая отвертка",
            true,
            null
    );

    private final ItemBookingDto itemBookingDto = new ItemBookingDto(
            1L,
            "Отвертка",
            "лучшая отвертка",
            true,
            null,
            null,
            new ArrayList<>());

    private final Request request = new Request(
            1L,
            "нужна отвертка",
            secondUser,
            LocalDateTime.of(2022, 11, 11, 12, 0)
    );

    private final Item itemWithRequest = new Item(
            1L,
            "Отвертка",
            "лучшая отвертка",
            user, true,
            request
    );

    private final ItemDto itemDtoWithRequest = new ItemDto(
            1L,
            "Отвертка",
            "лучшая отвертка",
            true,
            1L
    );

    private final Comment comment = new Comment(
            1L,
            "хорошая отвертка",
            item,
            secondUser,
            LocalDateTime.of(2022, 11, 30, 11, 0)
    );

    private final CommentDto commentDto = new CommentDto(
            1L,
            "хорошая отвертка",
            "test",
            LocalDateTime.of(2022, 11, 30, 11, 0)
    );

    private final Booking booking = new Booking(
            1L,
            item,
            secondUser,
            LocalDateTime.of(2022, 9, 9, 14, 0),
            LocalDateTime.of(2022, 9, 9, 18, 0),
            BookingStatus.APPROVED
    );

    @Test
    void addItem() {
        Mockito.when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(itemRepository.saveAndFlush(item))
                .thenReturn(item);

        ItemDto createdItemDto = itemService.addItem(itemDtoWithId, 1L);

        assertEquals(itemDtoWithId, createdItemDto);
        Mockito.verify(itemRepository, Mockito.times(1))
                .saveAndFlush(item);
    }

    @Test
    void addItemWrongUser() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.addItem(itemDto, 1L));
        assertEquals("Пользователь с id=1 не найден", notFoundException.getMessage());
    }

    @Test
    void addItemWithRequest() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(1L))
                .thenReturn(Optional.of(request));
        Mockito.when(itemRepository.saveAndFlush(itemWithRequest))
                .thenReturn(itemWithRequest);

        ItemDto createdItemDto = itemService.addItem(itemDtoWithRequest, 1L);
        assertEquals(itemDtoWithRequest, createdItemDto);
    }

    @Test
    void addItemUnknownRequest() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.addItem(itemDtoWithRequest, 1L));
        assertEquals("Запрос с id=1 не найден", notFoundException.getMessage());
    }

    @Test
    void addComment() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllBookingsOfItemAndOwner(anyLong(), anyLong(), any()))
                .thenReturn(List.of(booking));
        Mockito.when(commentRepository.saveAndFlush(comment))
                .thenReturn(comment);

        CommentDto createdComment = itemService.addComment(1L, 2L, commentDto);

        assertEquals(commentDto, createdComment);
    }

    @Test
    void addCommentUnknownItem() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.addComment(1L, 1L, commentDto));
        assertEquals("Вещь с id=1 не найдена", notFoundException.getMessage());
    }

    @Test
    void addCommentUnknownAuthor() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.addComment(1L, 1L, commentDto));
        assertEquals("Пользователь с id=1 не найден", notFoundException.getMessage());
    }

    @Test
    void addCommentNoBookings() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllBookingsOfItemAndOwner(anyLong(), anyLong(), any()))
                .thenReturn(List.of());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> itemService.addComment(1L, 1L, commentDto));

        assertEquals("Пользователь не может оставить отзыв к вещи, которую не бронировал",
                badRequestException.getMessage());
    }
    @Test
    void editItem() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        ItemDto itemDtoToUpdate = new ItemDto(
                1L,
                "Отвертка test",
                "лучшая отвертка",
                false,
                null
        );
        Item itemToUpdate = new Item(
                1L,
                "Отвертка test",
                "лучшая отвертка",
                user,
                false,
                null
        );
        Mockito.when(itemRepository.saveAndFlush(itemToUpdate))
                .thenReturn(itemToUpdate);

        ItemDto editedItem = itemService.editItem(1L, itemDtoToUpdate, 1L);
        assertEquals(itemDtoToUpdate, editedItem);
        Mockito.verify(itemRepository, Mockito.times(1))
                .saveAndFlush(itemToUpdate);
    }

    @Test
    void editItemUnknownItem() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.editItem(1L, itemDto, 1L));

        assertEquals("Вещь с id=1 не найдена", notFoundException.getMessage());

    }

    @Test
    void editItemForbiddenException() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> itemService.editItem(1L, itemDto, 2L));

        assertEquals("Обновление владельца вещи невозможно", forbiddenException.getMessage());

    }

    @Test
    void getItem() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItem_Id(anyLong()))
                .thenReturn(List.of());
        Mockito.when(commentRepository.findAllByItem_Id(anyLong()))
                .thenReturn(List.of());


        ItemBookingDto bookingDto = itemService.getItem(1L, 1L);

        assertEquals(itemBookingDto, bookingDto);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
    }

    @Test
    void getUnknownItem() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getItem(1L,1L));

        assertEquals("Вещь с id=1 не найдена", notFoundException.getMessage());
    }
    @Test
    void getAllItems() {
        CustomPageRequest pageRequest = new CustomPageRequest(0, 10);
        Mockito.when(itemRepository.findAllByOwner_Id(1L, pageRequest))
                .thenReturn(List.of(item));
        Mockito.when(bookingRepository.findAllByItem_Id(anyLong()))
                .thenReturn(List.of());
        Mockito.when(commentRepository.findAllByItem_Id(anyLong()))
                .thenReturn(List.of());

        List<ItemBookingDto> itemBookingDtos = itemService.getAllItems(1L, 0, 10);
        assertEquals(List.of(itemBookingDto).get(0).getId(), itemBookingDtos.get(0).getId());
        assertEquals(List.of(itemBookingDto).size(), itemBookingDtos.size());

        Mockito.verify(itemRepository,
                        Mockito.times(1))
                .findAllByOwner_Id(1L, pageRequest);

        Mockito.when(itemRepository.findAllByOwner_Id(2L, pageRequest))
                .thenReturn(List.of());
        List<ItemBookingDto> emptyItems = itemService.getAllItems(2L, 0, 10);
        assertEquals(0, emptyItems.size());

    }

    @Test
    void searchItems() {
        CustomPageRequest pageRequest = new CustomPageRequest(0, 10);
        Mockito.when(itemRepository.search("отВерт", pageRequest))
                .thenReturn(List.of(item));

        List<ItemDto> itemDtos = itemService.searchItems("отВерт", 0, 10);
        assertEquals(List.of(itemDtoWithId).get(0).getId(), itemDtos.get(0).getId());
        assertEquals(List.of(itemDto).size(), itemDtos.size());

        Mockito.verify(itemRepository,
                        Mockito.times(1))
                .search("отВерт", pageRequest);

        Mockito.when(itemRepository.search("kkk", pageRequest))
                .thenReturn(List.of());

        List<ItemDto> emptyItemDtos = itemService.searchItems("kkk", 0, 10);
        assertEquals(0, emptyItemDtos.size());
    }

    @Test
    void deleteItem() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        itemService.deleteItem(1L, 1L);

        Mockito.verify(itemRepository, Mockito.times(1))
                .deleteById(1L);
    }

    @Test
    void deleteItemForbidden() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class, () ->
           itemService.deleteItem(1L, 2L)
        );

        assertEquals("У пользователя с id=2 нет права на удаление вещи с id=1",
                forbiddenException.getMessage());
    }

    @Test
    void deleteUnknownItem() {
        Mockito.when(itemRepository.findById(2L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.deleteItem(2L, 2L));
        assertEquals("Вещь с id=2 не найдена", notFoundException.getMessage());
    }
}
