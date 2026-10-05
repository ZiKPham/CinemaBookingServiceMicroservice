package com.cinema.booking_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cinema.booking_service.domain.Ticket;
import com.cinema.booking_service.util.constant.BookingStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

        // @Query("SELECT t.seat.id FROM Ticket t " +
        // "WHERE t.booking.showtime.id = :showtimeId " +
        // "AND t.seat.id IN :seatIds " +
        // "AND t.booking.status IN :statuses")
        // List<Long> findBookedSeatIds(
        // @Param("showtimeId") Long showtimeId,
        // @Param("seatIds") List<Long> seatId,
        // @Param("Statuses") List<BookingStatus> statuses);

        @Query("SELECT t.seatId FROM Ticket t JOIN t.booking b " +
                        "WHERE b.showtimeId = :showtimeId " +
                        "AND t.seatId IN :seatIds " +
                        "AND b.status IN :statuses")
        List<Long> findBookedSeatIds(
                        @Param("showtimeId") Long showtimeId,
                        @Param("seatIds") List<Long> seatIds,
                        @Param("statuses") List<BookingStatus> statuses);

        // Sửa lại đoạn JOIN này cho đúng cấu trúc entity của bạn
        // @Query("SELECT t.seat.id FROM Ticket t JOIN t.booking b JOIN b.showtime s " +
        // "WHERE s.id = :showtimeId AND b.status IN (:statuses)")
        // List<Long> findBookedSeatIdsByShowtimeId(
        // @Param("showtimeId") Long showtimeId,
        // @Param("statuses") List<BookingStatus> statuses);
}
