package com.cinema.cinema_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.cinema.cinema_service.domain.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    List<Room> findByCinemaNameContainingIgnoreCaseAndNameContainingIgnoreCase(String cinemaName, String roomName);

    List<Room> findByCinemaNameContainingIgnoreCase(String cinemaName);

    List<Room> findByNameContainingIgnoreCase(String name);
}
