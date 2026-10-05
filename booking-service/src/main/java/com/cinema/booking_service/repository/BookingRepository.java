package com.cinema.booking_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.util.constant.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByUserEmailOrderByIdDesc(String userEmail);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, Instant time);

    List<Booking> findByUserEmail(String userEmail);

    Optional<Booking> findByIdAndUserEmail(Long id, String userEmail);
}
