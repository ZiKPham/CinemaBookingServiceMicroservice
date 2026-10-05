package com.cinema.showtime_service.service;

import java.time.Instant;
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

import com.cinema.showtime_service.client.BookingClient;
import com.cinema.showtime_service.client.CinemaClient;
import com.cinema.showtime_service.client.MovieClient;
import com.cinema.showtime_service.domain.Showtime;
import com.cinema.showtime_service.domain.request.ReqCreateShowtimeDTO;
import com.cinema.showtime_service.domain.request.ReqUpdateShowtimeDTO;
import com.cinema.showtime_service.domain.request.ShowtimeSearchCriteria;
import com.cinema.showtime_service.domain.response.ResMovieDTO;
import com.cinema.showtime_service.domain.response.ResRoomDTO;
import com.cinema.showtime_service.domain.response.ResSeatDTO;
import com.cinema.showtime_service.domain.response.ResShowtimeDTO;
import com.cinema.showtime_service.domain.response.RestResponse;
import com.cinema.showtime_service.domain.response.ResultPaginationDTO;
import com.cinema.showtime_service.repository.ShowtimeRepository;
import com.cinema.showtime_service.util.error.IdInvalidException;

import jakarta.persistence.criteria.Predicate;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final CinemaClient cinemaClient;
    private final BookingClient bookingClient;
    private final MovieClient movieClient;

    public ShowtimeService(ShowtimeRepository showtimeRepository, CinemaClient cinemaClient,
            BookingClient bookingClient, MovieClient movieClient) {
        this.showtimeRepository = showtimeRepository;
        this.cinemaClient = cinemaClient;
        this.bookingClient = bookingClient;
        this.movieClient = movieClient;
    }

    @CacheEvict(value = "showtimes", allEntries = true)
    public ResShowtimeDTO handleCreateShowtime(ReqCreateShowtimeDTO reqDTO) throws IdInvalidException {
        try {
            ResMovieDTO movieInfo = movieClient.getMovieById(reqDTO.getMovieId());
            if (movieInfo == null) {
                throw new IdInvalidException("Movie với id = " + reqDTO.getMovieId() + " không tồn tại");
            }
        } catch (Exception e) {
            throw new IdInvalidException(
                    "Movie với id = " + reqDTO.getMovieId() + " không tồn tại hoặc hệ thống phim đang bận!");
        }

        try {
            ResRoomDTO roomInfo = cinemaClient.getRoomById(reqDTO.getRoomId());
            if (roomInfo == null) {
                throw new IdInvalidException("Room với id = " + reqDTO.getRoomId() + " không tồn tại");
            }
        } catch (Exception e) {
            throw new IdInvalidException(
                    "Room với id = " + reqDTO.getRoomId() + " không tồn tại hoặc hệ thống rạp đang bận!");
        }

        if (!reqDTO.getStartTime().isBefore(reqDTO.getEndTime())) {
            throw new IdInvalidException("Thời gian bắt đầu " + reqDTO.getStartTime()
                    + " phải trước thời gian kết thúc " + reqDTO.getEndTime());
        }

        List<Showtime> overlaps = this.showtimeRepository.findOverlappingShowtimes(
                reqDTO.getRoomId(), reqDTO.getStartTime(), reqDTO.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new IdInvalidException("Khung giờ này phòng đã có suất chiếu khác. Vui lòng chọn giờ khác!");
        }

        Showtime showtime = new Showtime();
        showtime.setStartTime(reqDTO.getStartTime());
        showtime.setEndTime(reqDTO.getEndTime());
        showtime.setPrice(reqDTO.getPrice());
        showtime.setMovieId(reqDTO.getMovieId());
        showtime.setRoomId(reqDTO.getRoomId());

        Showtime saved = this.showtimeRepository.save(showtime);
        return this.convertToResShowtimeDTO(saved);
    }

    @Cacheable(value = "showtimes")
    public ResultPaginationDTO fetchAllShowtimes(ShowtimeSearchCriteria criteria, Pageable pageable) {
        Specification<Showtime> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo ID phim
            if (criteria.getMovieId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("movieId"), criteria.getMovieId()));
            }

            // 2. Lọc theo ID phòng chiếu
            if (criteria.getRoomId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("roomId"), criteria.getRoomId()));
            }

            // 3. Lọc suất chiếu từ thời điểm...
            if (criteria.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), criteria.getFromDate()));
            }

            // 4. Lọc suất chiếu đến thời điểm...
            if (criteria.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), criteria.getToDate()));
            }

            // 5. Lọc theo giá vé từ mức...
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }

            // 6. Lọc theo giá vé đến mức...
            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Showtime> pageShowtime = this.showtimeRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageShowtime.getTotalPages());
        mt.setTotal(pageShowtime.getTotalElements());

        rs.setMeta(mt);

        List<ResShowtimeDTO> listShowtime = pageShowtime.getContent().stream()
                .map(this::convertToResShowtimeDTO)
                .collect(Collectors.toList());

        rs.setResult(listShowtime);
        return rs;
    }

    @Cacheable(value = "showtime-detail", key = "#p0")
    public ResShowtimeDTO fetchShowtimeById(Long id) throws IdInvalidException {
        Optional<Showtime> sOptional = this.showtimeRepository.findById(id);
        if (!sOptional.isPresent()) {
            throw new IdInvalidException("Showtime với id = " + id + " không tồn tại");
        }
        return this.convertToResShowtimeDTO(sOptional.get());
    }

    @CacheEvict(value = { "showtime-detail", "showtime-seats" }, key = "#p0")
    public ResShowtimeDTO handleUpdateShowtime(long id, ReqUpdateShowtimeDTO reqDTO) throws IdInvalidException {
        Optional<Showtime> sOptional = this.showtimeRepository.findById(id);
        if (!sOptional.isPresent()) {
            throw new IdInvalidException("Showtime với id = " + id + " không tồn tại");
        }

        Showtime currentShowtime = sOptional.get();

        // 1. Xác định thông tin mới (nếu client không truyền lên thì lấy lại giá trị cũ
        // trong DB)
        Instant targetStartTime = reqDTO.getStartTime() != null ? reqDTO.getStartTime()
                : currentShowtime.getStartTime();
        Instant targetEndTime = reqDTO.getEndTime() != null ? reqDTO.getEndTime()
                : currentShowtime.getEndTime();
        Long targetRoomId = reqDTO.getRoomId() != null ? reqDTO.getRoomId()
                : currentShowtime.getRoomId();
        Long targetMovieId = reqDTO.getMovieId() != null ? reqDTO.getMovieId()
                : currentShowtime.getMovieId();
        Double targetPrice = reqDTO.getPrice() != null ? reqDTO.getPrice()
                : currentShowtime.getPrice();

        // 2. Kiểm tra logic thời gian bắt đầu phải trước kết thúc
        if (!targetStartTime.isBefore(targetEndTime)) {
            throw new IdInvalidException("Thời gian bắt đầu phải diễn ra trước thời gian kết thúc");
        }

        // 3. Kiểm tra trùng lịch suất chiếu (dùng id hiện tại để loại trừ chính nó)
        List<Showtime> overlaps = this.showtimeRepository.findOverLappingShowtimesForUpdate(
                id, targetRoomId, targetStartTime, targetEndTime);
        if (!overlaps.isEmpty()) {
            throw new IdInvalidException("Khung giờ này phòng đã có suất chiếu khác. Vui lòng chọn giờ khác!");
        }

        // 4. Gán dữ liệu vào entity
        currentShowtime.setStartTime(targetStartTime);
        currentShowtime.setEndTime(targetEndTime);
        currentShowtime.setPrice(targetPrice);
        currentShowtime.setMovieId(targetMovieId);
        currentShowtime.setRoomId(targetRoomId);

        Showtime updated = this.showtimeRepository.save(currentShowtime);
        return this.convertToResShowtimeDTO(updated);
    }

    @CacheEvict(value = { "showtime-detail", "showtime-seats" }, key = "#p0")
    public void handleDeleteShowtime(long id) throws IdInvalidException {
        Optional<Showtime> showtimeOpt = this.showtimeRepository.findById(id);
        if (!showtimeOpt.isPresent()) {
            throw new IdInvalidException("Showtime với id = " + id + " không tồn tại");
        }
        this.showtimeRepository.deleteById(id);
    }

    @Cacheable(value = "showtime-seats", key = "#p0")
    public List<ResSeatDTO> getSeatMapByShowtime(long showtimeId) throws IdInvalidException {
        // 1. Lấy thông tin suất chiếu
        Showtime showtime = this.showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IdInvalidException("Suất chiếu không tồn tại với ID: " + showtimeId));

        long roomId = showtime.getRoomId();

        // 2. Gọi sang cinema-service để lấy toàn bộ ghế của phòng chiếu
        List<ResSeatDTO> allSeats;
        try {
            RestResponse<List<ResSeatDTO>> res = cinemaClient.getSeatsByRoomId(roomId, showtimeId);

            if (res == null) {
                throw new IdInvalidException(
                        "Cinema-service trả về response null!");
            }
            allSeats = res.getData();
        } catch (Exception e) {
            throw new IdInvalidException(
                    "Không thể lấy danh sách ghế từ hệ thống rạp chiếu!");
        }

        // Đảm bảo list không null để tránh NullPointerException
        if (allSeats == null) {
            allSeats = new ArrayList<>();
        }

        // 3. Gọi sang booking-service để lấy danh sách các ghế đã bị đặt/giữ chỗ
        List<Long> bookedSeatIds;
        try {
            bookedSeatIds = bookingClient.getBookedSeatIds(showtimeId);
        } catch (Exception e) {
            // Fallback an toàn nếu booking-service bận (coi như chưa có ghế nào bị đặt)
            bookedSeatIds = new ArrayList<>();
        }

        // 4. Duyệt qua danh sách ghế và gán cờ isBooked
        List<Long> finalBookedSeatIds = bookedSeatIds;
        return allSeats.stream().map(seat -> {
            boolean isBooked = finalBookedSeatIds.contains(seat.getId());
            seat.setBooked(isBooked);
            return seat;
        }).collect(Collectors.toList());
    }

    public ResShowtimeDTO convertToResShowtimeDTO(Showtime showtime) {
        ResShowtimeDTO res = new ResShowtimeDTO();
        res.setId(showtime.getId());
        res.setStartTime(showtime.getStartTime());
        res.setEndTime(showtime.getEndTime());
        res.setPrice(showtime.getPrice());
        res.setCreatedAt(showtime.getCreatedAt());
        res.setUpdatedAt(showtime.getUpdatedAt());

        // 1. Lấy thông tin Movie qua Feign Client để điền tên phim
        try {
            ResMovieDTO movieInfo = movieClient.getMovieById(showtime.getMovieId());
            if (movieInfo != null) {
                ResShowtimeDTO.MovieShowtime m = new ResShowtimeDTO.MovieShowtime();
                m.setId(showtime.getMovieId());
                m.setName(movieInfo.getName()); // Bổ sung tên phim
                res.setMovie(m);
            }
        } catch (Exception e) {
            // Fallback nếu movie-service bận, ít nhất vẫn gán được ID
            ResShowtimeDTO.MovieShowtime m = new ResShowtimeDTO.MovieShowtime();
            m.setId(showtime.getMovieId());
            res.setMovie(m);
        }

        // 2. Lấy thông tin Room và Cinema qua Feign Client để điền tên phòng, tên rạp
        try {
            ResRoomDTO roomInfo = cinemaClient.getRoomById(showtime.getRoomId());
            if (roomInfo != null) {
                ResShowtimeDTO.RoomShowtime r = new ResShowtimeDTO.RoomShowtime();
                r.setId(showtime.getRoomId());
                r.setName(roomInfo.getName()); // Bổ sung tên phòng
                if (roomInfo.getCinema() != null) {
                    r.setCinemaName(roomInfo.getCinema().getName()); // Bổ sung tên rạp
                }
                res.setRoom(r);
            }
        } catch (Exception e) {
            // Fallback nếu cinema-service bận
            ResShowtimeDTO.RoomShowtime r = new ResShowtimeDTO.RoomShowtime();
            r.setId(showtime.getRoomId());
            res.setRoom(r);
        }

        return res;
    }
}
