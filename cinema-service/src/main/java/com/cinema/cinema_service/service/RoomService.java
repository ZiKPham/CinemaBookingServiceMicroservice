package com.cinema.cinema_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cinema.cinema_service.domain.Cinema;
import com.cinema.cinema_service.domain.Room;
import com.cinema.cinema_service.domain.request.ReqCreateRoomDTO;
import com.cinema.cinema_service.domain.request.ReqUpdateRoomDTO;
import com.cinema.cinema_service.domain.request.RoomSearchCriteria;
import com.cinema.cinema_service.domain.response.ResRoomDTO;
import com.cinema.cinema_service.domain.response.ResultPaginationDTO;
import com.cinema.cinema_service.repository.CinemaRepository;
import com.cinema.cinema_service.repository.RoomRepository;
import com.cinema.cinema_service.util.error.IdInvalidException;

import jakarta.persistence.criteria.Predicate;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;

    public RoomService(RoomRepository roomRepository, CinemaRepository cinemaRepository) {
        this.roomRepository = roomRepository;
        this.cinemaRepository = cinemaRepository;
    }

    @CacheEvict(value = "room-detail", allEntries = true)
    public ResRoomDTO handleCreateRoom(ReqCreateRoomDTO reqDTO) throws IdInvalidException {
        Optional<Cinema> cOptional = this.cinemaRepository.findById(reqDTO.getCinemaId());
        if (!cOptional.isPresent()) {
            throw new IdInvalidException("Cinema với id = " + reqDTO.getCinemaId() + " không tồn tại");
        }

        Room room = new Room();
        room.setName(reqDTO.getName());
        room.setTotalSeats(reqDTO.getTotalSeats());
        room.setCinema(cOptional.get());

        Room savedRoom = this.roomRepository.save(room);
        return this.convertToResRoomDTO(savedRoom);
    }

    @Cacheable(value = "room-detail", key = "#p0")
    public ResRoomDTO fetchRoomById(long id) throws IdInvalidException {
        Optional<Room> roomOptional = this.roomRepository.findById(id);
        if (!roomOptional.isPresent()) {
            throw new IdInvalidException("Room với id = " + id + " không tồn tại");
        }

        Room room = roomOptional.get();
        return this.convertToResRoomDTO(room); // Hàm chuyển đổi Entity sang DTO tương ứng của bạn
    }

    public ResultPaginationDTO fetchAllRooms(RoomSearchCriteria criteria, Pageable pageable) {
        Specification<Room> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo tên phòng (tìm kiếm gần đúng - LIKE)
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"));
            }

            // Lọc theo số lượng ghế
            if (criteria.getTotalSeats() != null) {
                predicates.add(criteriaBuilder.equal(root.get("totalSeats"), criteria.getTotalSeats()));
            }

            // Lọc theo rạp chiếu phim (Cinema ID thông qua mối quan hệ @ManyToOne)
            if (criteria.getCinemaId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("cinema").get("id"), criteria.getCinemaId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Thực hiện query phân trang với Specification
        Page<Room> pageRoom = this.roomRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageRoom.getTotalPages());
        mt.setTotal(pageRoom.getTotalElements());

        rs.setMeta(mt);

        List<ResRoomDTO> rooms = pageRoom.getContent().stream().map(this::convertToResRoomDTO)
                .collect(Collectors.toList());

        rs.setResult(rooms);
        return rs;
    }

    public List<ResRoomDTO> fetchRoomsByCinemaAndRoomName(String cinemaName, String roomName) {
        List<Room> rooms;

        boolean hasCinema = cinemaName != null && !cinemaName.trim().isEmpty();
        boolean hasRoom = roomName != null && !roomName.trim().isEmpty();

        if (hasCinema && hasRoom) {
            rooms = this.roomRepository.findByCinemaNameContainingIgnoreCaseAndNameContainingIgnoreCase(
                    cinemaName.trim(), roomName.trim());
        } else if (hasCinema) {
            rooms = this.roomRepository.findByCinemaNameContainingIgnoreCase(cinemaName.trim());
        } else if (hasRoom) {
            // Nếu chỉ truyền vào tên phòng mà không truyền tên rạp
            rooms = this.roomRepository.findByNameContainingIgnoreCase(roomName.trim());
        } else {
            // Nếu không truyền gì cả, trả về toàn bộ hoặc danh sách rỗng tùy nghiệp vụ
            rooms = this.roomRepository.findAll();
        }
        return rooms.stream()
                .map(this::convertToResRoomDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "room-detail", key = "#p0")
    public ResRoomDTO handleUpdateRoom(long id, ReqUpdateRoomDTO reqDTO) throws IdInvalidException {
        Optional<Room> rOptional = this.roomRepository.findById(id);
        if (!rOptional.isPresent()) {
            throw new IdInvalidException("Room với id = " + id + " không tồn tại");
        }

        Room currentRoom = rOptional.get();
        if (reqDTO.getName() != null && !reqDTO.getName().trim().isEmpty()) {
            currentRoom.setName(reqDTO.getName());
        }

        if (reqDTO.getTotalSeats() != null) {
            currentRoom.setTotalSeats(reqDTO.getTotalSeats());
        }

        if (reqDTO.getCinemaId() != null) {
            Optional<Cinema> cinemaOptional = this.cinemaRepository.findById(reqDTO.getCinemaId());
            if (!cinemaOptional.isPresent()) {
                throw new IdInvalidException("Cinema với id = " + reqDTO.getCinemaId() + " không tồn tại");
            }
            currentRoom.setCinema(cinemaOptional.get());
        }

        Room updatedRoom = this.roomRepository.save(currentRoom);
        return this.convertToResRoomDTO(updatedRoom);
    }

    @CacheEvict(value = "room-detail", key = "#p0")
    public void handleDeleteRoom(long id) throws IdInvalidException {
        Optional<Room> roomOptional = this.roomRepository.findById(id);
        if (!roomOptional.isPresent()) {
            throw new IdInvalidException("Room với id = " + id + " không tồn tại");
        }
        this.roomRepository.deleteById(id);
    }

    public ResRoomDTO convertToResRoomDTO(Room room) {
        ResRoomDTO res = new ResRoomDTO();
        res.setId(room.getId());
        res.setName(room.getName());
        res.setTotalSeats(room.getTotalSeats());
        res.setCreatedAt(room.getCreatedAt());
        res.setUpdatedAt(room.getUpdatedAt());

        if (room.getCinema() != null) {
            ResRoomDTO.CinemaRoom cinemaRoom = new ResRoomDTO.CinemaRoom();
            cinemaRoom.setId(room.getCinema().getId());
            cinemaRoom.setName(room.getCinema().getName());
            res.setCinema(cinemaRoom);
        }

        return res;
    }
}
