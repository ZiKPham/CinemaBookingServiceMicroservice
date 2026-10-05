package com.cinema.booking_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.cinema.booking_service.domain.request.ReqHoldSeatDTO;
import com.cinema.booking_service.domain.response.ResSeatDTO;
import com.cinema.booking_service.domain.response.ResSeatLockDetailDTO;
import com.cinema.booking_service.domain.response.RestResponse;

@FeignClient(name = "cinema-service")
public interface CinemaClient {

        @PostMapping("/api/v1/seats/hold")
        void holdSeats(@RequestHeader("X-User-Email") String userEmail,
                        @RequestBody ReqHoldSeatDTO request);

        @GetMapping("/api/v1/seats/room/{roomId}")
        RestResponse<List<ResSeatDTO>> getSeatsByRoomId(@PathVariable("roomId") Long roomId);

        @GetMapping("/api/v1/seats/lock/holder")
        RestResponse<ResSeatLockDetailDTO> getSeatHolder(
                        @RequestParam("showtimeId") Long showtimeId,
                        @RequestParam("seatId") Long seatId);;

        @DeleteMapping("/api/v1/seats/lock")
        void unlockSeat(@RequestParam("showtimeId") Long showtimeId, @RequestParam("seatId") Long seatId);
}
