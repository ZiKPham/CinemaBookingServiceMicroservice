package com.cinema.showtime_service.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cinema.showtime_service.domain.Showtime;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long>, JpaSpecificationExecutor<Showtime> {

        List<Showtime> findByRoomId(long roomId);

        @Query("SELECT s FROM Showtime s WHERE s.roomId = :roomId AND :startTime < s.endTime AND :endTime > s.startTime")
        List<Showtime> findOverlappingShowtimes(
                        @Param("roomId") Long roomId,
                        @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime);

        @Query("SELECT s FROM Showtime s WHERE s.roomId = :roomId AND s.id != :id AND :startTime < s.endTime AND :endTime > s.startTime")
        List<Showtime> findOverLappingShowtimesForUpdate(
                        @Param("roomId") Long roomId,
                        @Param("id") Long id,
                        @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime);
}
