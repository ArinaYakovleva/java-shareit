package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_IdOrderByEndDesc(Long bookerId, Pageable pageable);

    List<Booking> findAllByItem_Owner_IdOrderByEndDesc(Long ownerId, Pageable pageable);

    List<Booking> findAllByItem_Id(Long itemId);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.start <= ?2 " +
            "and b.end >= ?2 " +
            "order by b.end desc ")
    List<Booking> findAllCurrentByBooker(Long bookerId, LocalDateTime currentDate, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.booker.id= ?1 " +
            "and b.end < ?2 " +
            "order by b.end desc ")
    List<Booking> findAllPastByBooker(Long bookerId, LocalDateTime currentDate, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.booker.id= ?1 " +
            "and b.end > ?2 " +
            "order by b.end desc")
    List<Booking> findAllFutureByBooker(Long bookerId, LocalDateTime currentDate, Pageable pageable);

    List<Booking> findAllByBooker_IdAndStatusOrderByEnd(Long bookerId, BookingStatus status, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.owner.id= ?1 " +
            "and b.start <= ?2 " +
            "and b.end >= ?2 " +
            "order by b.end desc")
    List<Booking> findAllCurrentByOwner(Long ownerId, LocalDateTime currentDate, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.owner.id= ?1 " +
            "and b.end < ?2 " +
            "order by b.end desc ")
    List<Booking> findAllPastByOwner(Long ownerId, LocalDateTime currentDate, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.end > ?2 " +
            "order by b.end desc")
    List<Booking> findAllFutureByOwner(Long ownerId, LocalDateTime currentDate, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.booker.id = ?2 " +
            "and b.end < ?3 ")
    List<Booking> findAllBookingsOfItemAndOwner(Long itemId, Long bookerId, LocalDateTime currentDate);

    List<Booking> findAllByItem_Owner_IdAndStatusOrderByEndDesc(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.start between ?2 and ?3 " +
            "and b.end between ?2 and ?3 ")
    List<Booking> findAllOverlappedBookings(Long itemId, LocalDateTime start, LocalDateTime end);

}
