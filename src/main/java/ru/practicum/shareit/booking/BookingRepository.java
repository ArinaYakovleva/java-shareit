package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_IdOrderByEndDesc(Long bookerId);

    List<Booking> findAllByItem_Owner_IdOrderByEndDesc(Long ownerId);

    List<Booking> findAllByItem_Id(Long itemId);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.start <= ?2 " +
            "and b.end >= ?2 " +
            "order by b.end desc ")
    List<Booking> findAllCurrentByBooker(Long bookerId, LocalDateTime currentDate);

    @Query("select b from Booking b " +
            "where b.booker.id= ?1 " +
            "and b.end < ?2 " +
            "order by b.end desc ")
    List<Booking> findAllPastByBooker(Long bookerId, LocalDateTime currentDate);

    @Query("select b from Booking b " +
            "where b.booker.id= ?1 " +
            "and b.end > ?2 " +
            "order by b.end desc")
    List<Booking> findAllFutureByBooker(Long bookerId, LocalDateTime currentDate);

    List<Booking> findAllByBooker_IdAndStatusOrderByEnd(Long bookerId, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.item.owner.id= ?1 " +
            "and b.start <= ?2 " +
            "and b.end >= ?2 " +
            "order by b.end desc")
    List<Booking> findAllCurrentByOwner(Long ownerId, LocalDateTime currentDate);

    @Query("select b from Booking b " +
            "where b.item.owner.id= ?1 " +
            "and b.end < ?2 " +
            "order by b.end desc ")
    List<Booking> findAllPastByOwner(Long ownerId, LocalDateTime currentDate);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.end > ?2 " +
            "order by b.end desc")
    List<Booking> findAllFutureByOwner(Long ownerId, LocalDateTime currentDate);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.booker.id = ?2 " +
            "and b.end < ?3 ")
    List<Booking> findAllBookingsOfItemAndOwner(Long itemId, Long bookerId, LocalDateTime currentDate);

    List<Booking> findAllByItem_Owner_IdAndStatusOrderByEndDesc(Long ownerId, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.start > ?1 " +
            "and b.end < ?1 " +
            "and b.start > ?2 " +
            "and b.end < ?2")
    List<Booking> findAllOverlappedBookings(LocalDateTime start, LocalDateTime end);

}
