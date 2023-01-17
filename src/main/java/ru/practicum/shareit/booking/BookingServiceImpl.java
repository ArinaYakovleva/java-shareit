package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDTOMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ConfirmedBookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    @Override
    public BookingDto addBooking(CreateBookingDto bookingDto, Long bookerId) {
        User user = userRepository.findById(bookerId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id=%d не найден", bookerId);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> {
                    String errorMessage = String.format("Вещь с id=%d не найдена", bookingDto.getItemId());
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });

        checkDates(item.getId(), bookingDto.getStart(), bookingDto.getEnd());

        if (!item.getAvailable()) {
            String errorMessage = String.format("Вещь с id=%d не доступна для бронирования", item.getId());
            log.error(errorMessage);
            throw new BadRequestException(errorMessage);
        }


        if (item.getOwner().getId().equals(bookerId)) {
            String ownerValidation = "Владелец вещи не может бронировать свою вещь";
            log.error(ownerValidation);
            throw new NotFoundException(ownerValidation);
        }

        Booking booking = repository.saveAndFlush(BookingDTOMapper
                .fromCreateBookingDto(bookingDto, user, item, BookingStatus.WAITING));
        log.info(String.format("Добавление бронирование: %s", booking));
        return BookingDTOMapper.toBookingDto(booking);
    }

    @Override
    public ConfirmedBookingDto confirmBooking(Long bookingId, Long userId, Boolean isApproved) {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Бронирование с id=%d не найдено", bookingId);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            String errorMessage = String.format("Пользователь с id=%d не может подтвердить бронирование", userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        if (booking.getStatus() == BookingStatus.APPROVED) {
            String errorMessage = String.format("Невозможно изменить статус одобренного бронирования с id=%d", bookingId);
            log.error(errorMessage);
            throw new BadRequestException(errorMessage);
        }

        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info(String.format("Подтверждение бронирования с id=%d: %b", bookingId, isApproved));
        return BookingDTOMapper.toConfirmedBookingDto(repository.saveAndFlush(booking));
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Бронирование с id=%d не найдено", bookingId);
                    log.error(errorMessage);
                    throw new NotFoundException(errorMessage);
                });

        boolean isOwnerOrBooker = booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId);
        if (!isOwnerOrBooker) {
            throw new NotFoundException(String.format("Пользователь с id=%d не может просматривать это бронирование", userId));
        }
        return BookingDTOMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookings(String state, Long userId, boolean isByOwner, Integer from, Integer size) {
        CustomPageRequest pageRequest = new CustomPageRequest(from, size);
        BookingState stateValue;
        try {
            stateValue = BookingState.valueOf(state);
        } catch (Exception e) {
            String errorMessage = String.format("Unknown state: %s", state);
            log.error(errorMessage);
            throw new BadRequestException(errorMessage);
        }

        if (userRepository.findById(userId).isEmpty()) {
            String errorMessage = String.format("Пользователь с id=%d не найден", userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        if (isByOwner) {
            return getBookingsByOwner(stateValue, userId, pageRequest);
        }

        return getBookingsByBooker(stateValue, userId, pageRequest);
    }

    private List<BookingDto> getBookingsByOwner(BookingState state, Long userId, Pageable pageable) {
        List<BookingDto> bookings;
        switch (state) {
            case CURRENT:
                bookings = repository.findAllCurrentByOwner(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = repository.findAllPastByOwner(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = repository.findAllFutureByOwner(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case WAITING:
                bookings = repository
                        .findAllByItem_Owner_IdAndStatusOrderByEndDesc(userId, BookingStatus.WAITING, pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                bookings = repository
                        .findAllByItem_Owner_IdAndStatusOrderByEndDesc(userId, BookingStatus.REJECTED, pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            default:
                bookings = repository.findAllByItem_Owner_IdOrderByEndDesc(userId, pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
        }
        return bookings;
    }

    private List<BookingDto> getBookingsByBooker(BookingState state, Long userId, Pageable pageable) {
        List<BookingDto> bookings;
        switch (state) {
            case CURRENT:
                bookings = repository.findAllCurrentByBooker(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = repository.findAllPastByBooker(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = repository.findAllFutureByBooker(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case WAITING:
                bookings = repository
                        .findAllByBooker_IdAndStatusOrderByEnd(userId, BookingStatus.WAITING, pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                bookings = repository
                        .findAllByBooker_IdAndStatusOrderByEnd(userId, BookingStatus.REJECTED, pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            default:
                bookings = repository.findAllByBooker_IdOrderByEndDesc(userId, pageable).stream()
                        .map(BookingDTOMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
        }
        return bookings;
    }

    private void checkDates(Long itemId, LocalDateTime start, LocalDateTime end) {
        String message;
        if (end.isBefore(start)) {
            message = "Дата окончания бронирования не может быть раньше начала";
            log.error(message);
            throw new BadRequestException(message);
        }
        if (start.isBefore(LocalDateTime.now())) {
            message = "Дата начала бронирования не может быть в прошлом";
            log.error(message);
            throw new BadRequestException(message);
        }
        List<Booking> overlappingBookings = repository.findAllOverlappedBookings(itemId, start, end);
        if (!overlappingBookings.isEmpty()) {
            message = "Вещь недоступна для бронирования в эти даты";
            log.error(message);
            throw new BadRequestException(message);
        }
    }

}
