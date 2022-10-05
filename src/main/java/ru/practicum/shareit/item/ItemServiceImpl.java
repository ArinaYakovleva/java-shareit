package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDTOMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.ForbiddenException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDTOMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private static final String errorNotFound = "Вещь с id=%d не найдена";

    @Autowired
    public ItemServiceImpl(ItemRepository repository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id=%d не найден", ownerId);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });
        Item item = repository.saveAndFlush(ItemDTOMapper.fromItemDto(itemDto, owner));
        log.info(String.format("Добавление вещи: %s", item));
        return ItemDTOMapper.toItemDto(item);
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = repository
                .findById(itemId)
                .orElseThrow(() -> {
                    String errorMessage = String.format(errorNotFound, itemId);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });
        User author = userRepository
                .findById(userId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id=%d не найден", userId);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });

        boolean isNoPastBookings = bookingRepository
                .findAllBookingsOfItemAndOwner(itemId, userId, LocalDateTime.now()).isEmpty();

        if (isNoPastBookings) {
            throw new BadRequestException("Пользователь не может оставить отзыв к вещи, которую не бронировал");
        }

        Comment comment = commentRepository
                .saveAndFlush(ItemDTOMapper.fromCommentDto(commentDto, item, author, LocalDateTime.now()));

        log.info(String.format("Добавление комментария: %s", comment));
        return ItemDTOMapper.toCommentDto(comment);
    }

    @Override
    public ItemDto editItem(Long id, ItemDto item, Long ownerId) {
        Item existingItem = repository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format(errorNotFound, id);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            String errorMessage = "Обновление владельца вещи невозможно";
            log.error(errorMessage);
            throw new ForbiddenException(errorMessage);
        }
        User owner = userRepository.findById(existingItem.getOwner().getId())
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id=%d не найден", id);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });

        Item itemToUpdate = new Item(
                id,
                item.getName() == null ? existingItem.getName() : item.getName(),
                item.getDescription() == null ? existingItem.getDescription() : item.getDescription(),
                owner,
                item.getAvailable() == null ? existingItem.getAvailable() : item.getAvailable(),
                existingItem.getRequestId()
        );

        log.info(String.format("Изменение вещи с id=%d", id));

        return ItemDTOMapper.toItemDto(repository.saveAndFlush(itemToUpdate));
    }

    @Override
    public ItemBookingDto getItem(Long id, Long userId) {
        Item item = repository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format(errorNotFound, id);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });
        return makeItemBooking(item, userId);
    }

    @Override
    public void deleteItem(Long id, Long ownerId) {
        Item item = repository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format(errorNotFound, id);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });

        if (!item.getOwner().getId().equals(ownerId)) {
            String errorMessage = String.format("У пользователя с id=%d нет права на удаление" +
                    " вещи с id=%d", ownerId, id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        log.info(String.format("Удааление вещи с id=%d", id));
        repository.deleteById(id);
    }

    @Override
    public List<ItemBookingDto> getAllItems(Long ownerId) {
        return repository.findAllByOwner_Id(ownerId).stream()
                .map(item -> makeItemBooking(item, ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String searchStr) {
        if (searchStr == null || searchStr.isEmpty()) return new ArrayList<>();
        return repository.search(searchStr).stream()
                .map(ItemDTOMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private ItemBookingDto makeItemBooking(Item item, Long userId) {
        List<Booking> itemBookings = bookingRepository.findAllByItem_Id(item.getId());

        List<CommentDto> comments = commentRepository.findAllByItem_Id(item.getId()).stream()
                .map(ItemDTOMapper::toCommentDto)
                .collect(Collectors.toList());


        Booking lastBooking = itemBookings.stream()
                .filter(el -> el.getEnd().isBefore(LocalDateTime.now()))
                .findFirst()
                .orElse(null);

        Booking nextBooking = itemBookings.stream()
                .filter(el -> el.getEnd().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElse(null);

        boolean isItemsOwner = item.getOwner().getId().equals(userId);

        return ItemDTOMapper.toItemBookingDto(
                item,
                isItemsOwner ? BookingDTOMapper.toBookingItemDto(lastBooking) : null,
                isItemsOwner ? BookingDTOMapper.toBookingItemDto(nextBooking) : null,
                comments
        );
    }
}
