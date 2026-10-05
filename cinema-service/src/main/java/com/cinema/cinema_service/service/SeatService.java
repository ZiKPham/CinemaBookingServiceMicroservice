package com.cinema.cinema_service.service;

import com.cinema.cinema_service.domain.Room;
import com.cinema.cinema_service.domain.Seat;
import com.cinema.cinema_service.domain.request.ReqCreateSeatDTO;
import com.cinema.cinema_service.domain.request.ReqUpdateSeatDTO;
import com.cinema.cinema_service.domain.request.SeatSearchCriteria;
import com.cinema.cinema_service.domain.response.ResSeatDTO;
import com.cinema.cinema_service.domain.response.ResultPaginationDTO;
import com.cinema.cinema_service.repository.RoomRepository;
import com.cinema.cinema_service.repository.SeatRepository;
import com.cinema.cinema_service.util.constant.SeatType;
import com.cinema.cinema_service.util.error.IdInvalidException;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatLockService seatLockService;
    private final RoomRepository roomRepository;

    public SeatService(SeatRepository seatRepository, SeatLockService seatLockService, RoomRepository roomRepository) {
        this.seatRepository = seatRepository;
        this.seatLockService = seatLockService;
        this.roomRepository = roomRepository;
    }

    public ResSeatDTO handleCreateSeat(ReqCreateSeatDTO reqDTO) throws IdInvalidException {
        Room room = roomRepository.findById(reqDTO.getRoomId())
                .orElseThrow(() -> new IdInvalidException("Room với id = " + reqDTO.getRoomId() + " không tồn tại"));

        Seat seat = new Seat();
        seat.setSeatNumber(reqDTO.getSeatNumber());
        seat.setSeatType(reqDTO.getSeatType() != null ? reqDTO.getSeatType() : SeatType.STANDARD);
        seat.setRoom(room);

        Seat savedSeat = seatRepository.save(seat);
        return convertToResSeatDTO(savedSeat);
    }

    public ResSeatDTO handleUpdateSeat(ReqUpdateSeatDTO reqDTO) throws IdInvalidException {
        Seat currentSeat = seatRepository.findById(reqDTO.getId())
                .orElseThrow(() -> new IdInvalidException("Seat với id = " + reqDTO.getId() + " không tồn tại"));

        Room room = roomRepository.findById(reqDTO.getRoomId())
                .orElseThrow(() -> new IdInvalidException("Room với id = " + reqDTO.getRoomId() + " không tồn tại"));

        currentSeat.setSeatNumber(reqDTO.getSeatNumber());
        if (reqDTO.getSeatType() != null) {
            currentSeat.setSeatType(reqDTO.getSeatType());
        }
        currentSeat.setRoom(room);

        Seat updatedSeat = seatRepository.save(currentSeat);
        return convertToResSeatDTO(updatedSeat);
    }

    public void handleDeleteSeat(long id) throws IdInvalidException {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Seat với id = " + id + " không tồn tại"));
        seatRepository.delete(seat);
    }

    public ResultPaginationDTO fetchAllSeats(SeatSearchCriteria criteria, Pageable pageable) {
        Specification<Seat> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo seatNumber (Like '%value%')
            if (criteria.getSeatNumber() != null && !criteria.getSeatNumber().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("seatNumber")),
                        "%" + criteria.getSeatNumber().toLowerCase() + "%"));
            }

            // 2. Lọc theo seatType
            if (criteria.getSeatType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("seatType"), criteria.getSeatType()));
            }

            // 3. Lọc theo roomId
            if (criteria.getRoomId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("room"), criteria.getRoomId()));
            }

            // 4. Lọc theo cinemaId (thông qua quan hệ Room -> Cinema)
            if (criteria.getCinemaId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("room").get("cinema").get("id"), criteria.getCinemaId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Seat> pageSeat = this.seatRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPage(pageSeat.getTotalPages());
        mt.setTotal(pageSeat.getTotalElements());

        rs.setMeta(mt);

        List<ResSeatDTO> seats = pageSeat.getContent().stream().map(this::convertToResSeatDTO)
                .collect(Collectors.toList());

        rs.setResult(seats);

        return rs;
    }

    public ResSeatDTO fetchSeatById(long id) throws IdInvalidException {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Seat với id = " + id + " không tồn tại"));
        return convertToResSeatDTO(seat);
    }

    private ResSeatDTO convertToResSeatDTO(Seat seat) {
        ResSeatDTO dto = new ResSeatDTO();
        dto.setId(seat.getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setSeatType(seat.getSeatType());
        if (seat.getRoom() != null) {
            dto.setRoomId(seat.getRoom().getId());
            dto.setRoomName(seat.getRoom().getName());
        }
        dto.setCreatedAt(seat.getCreatedAt());
        dto.setUpdatedAt(seat.getUpdatedAt());
        return dto;
    }

    public List<ResSeatDTO> getSeatsByRoomIdAndShowtime(long roomId, Long showtimeId) {
        // 1. Lấy danh sách ghế tĩnh của phòng từ database
        List<Seat> seats = seatRepository.findByRoomId(roomId);

        // 2. Duyệt qua từng ghế và kết hợp check trạng thái khóa từ Redis (nếu có
        // showtimeId)
        return seats.stream().map(seat -> {
            boolean isBooked = (showtimeId != null) && seatLockService.isSeatLocked(showtimeId, seat.getId());
            return new ResSeatDTO(seat, isBooked);
        }).collect(Collectors.toList());
    }
}